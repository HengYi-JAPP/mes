package com.hengyi.japp.mes.auto.report.application.dto.silkException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.report.Report;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import reactor.core.publisher.Flux;

import java.util.*;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static com.mongodb.client.model.Filters.*;

/**
 * @author jzb 2019-10-12
 */
public class ExceptionRecordReport {
    private final String workshopId;
    private final long startDateTime;
    private final long endDateTime;
    @Getter
    private final Collection<GroupBy_Operator> groupByOperators;

    public ExceptionRecordReport(String workshopId, long startDateTime, long endDateTime, Flux<Document> exceptionRecord$) {
        this.workshopId = workshopId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        groupByOperators = exceptionRecord$.reduce(Maps.<String, GroupBy_Operator>newConcurrentMap(), (acc, cur) -> {
            final String lineMachineId = cur.getString("lineMachine");
            final Document lineMachine = QueryService.findFromCache(LineMachine.class, lineMachineId).get();
            final String lineId = lineMachine.getString("line");
            final Document line = QueryService.findFromCache(Line.class, lineId).get();
            if (Objects.equals(workshopId, line.getString("workshop"))) {
                acc.compute(cur.getString("creator"), (k, v) -> Optional.ofNullable(v).orElse(new GroupBy_Operator(k)).collect(cur));
            }
            return acc;
        }).map(Map::values).block();
    }

    public static ExceptionRecordReport create(String workshopId, long startDateTime, long endDateTime) {
        final MongoCollection<Document> T_ExceptionRecord = Report.mongoCollection(ExceptionRecord.class);
        final Bson startFilter = gte("cdt", new Date(startDateTime));
        final Bson endFilter = lte("cdt", new Date(endDateTime));
        final Flux<Document> exceptionRecord$ = Flux.from(T_ExceptionRecord.find(and(startFilter, endFilter)));
        return new ExceptionRecordReport(workshopId, startDateTime, endDateTime, exceptionRecord$);
    }

    public JsonNode toJsonNode() {
        final ArrayNode arrayNode = MAPPER.createArrayNode();
        J.emptyIfNull(groupByOperators).stream()
                .map(GroupBy_Operator::toJsonNode)
                .forEach(arrayNode::add);
        return arrayNode;
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class GroupBy_Operator {
        @EqualsAndHashCode.Include
        private final Operator operator = new Operator();
        private final Map<String, GroupBy_Batch> batchMap = Maps.newConcurrentMap();

        public GroupBy_Operator(String id) {
            final Document operator = QueryService.find(Operator.class, id).block();
            this.operator.setId(operator.getString(ID_COL));
            this.operator.setName(operator.getString("name"));
            this.operator.setHrId(operator.getString("hrId"));
        }

        public GroupBy_Operator collect(Document exceptionRecord) {
            final String silkId = exceptionRecord.getString("silk");
            final String batchId = QueryService.find(Silk.class, silkId).map(it -> it.getString("batch")).block();
            batchMap.compute(batchId, (k, v) -> {
                if (v == null) {
                    v = new GroupBy_Batch(batchId);
                }
                return v.collect(exceptionRecord);
            });
            return this;
        }

        public JsonNode toJsonNode() {
            final Collection<GroupBy_Product> products = Flux.fromIterable(batchMap.values())
                    .reduce(Maps.<Product, GroupBy_Product>newConcurrentMap(), (acc, cur) -> {
                        final Product product = cur.getBatch().getProduct();
                        acc.compute(product, (k, v) -> {
                            if (v == null) {
                                v = new GroupBy_Product(product);
                            }
                            return v.collect(cur);
                        });
                        return acc;
                    }).map(Map::values).block();
            final Map<String, Object> map = Map.of("operator", this.operator, "products", products);
            return MAPPER.convertValue(map, JsonNode.class);
        }
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class GroupBy_Batch {
        @EqualsAndHashCode.Include
        private final Batch batch = new Batch();
        private int silkCount = 0;

        public GroupBy_Batch(String batchId) {
            final Document batch = QueryService.findFromCache(Batch.class, batchId).get();
            this.batch.setId(batch.getString(ID_COL));
            this.batch.setBatchNo(batch.getString("batchNo"));
            this.batch.setSpec(batch.getString("spec"));
            this.batch.setSilkWeight(batch.getDouble("silkWeight"));
            final Product product = new Product();
            this.batch.setProduct(product);
            final Document document = QueryService.findFromCache(Product.class, batch.getString("product")).get();
            product.setId(document.getString(ID_COL));
            product.setName(document.getString("name"));
        }

        public GroupBy_Batch collect(Document exceptionRecord) {
            silkCount++;
            return this;
        }
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class GroupBy_Product {
        @EqualsAndHashCode.Include
        private final Product product;
        private int silkCount = 0;

        public GroupBy_Product collect(GroupBy_Batch collect) {
            silkCount += collect.silkCount;
            return this;
        }
    }
}
