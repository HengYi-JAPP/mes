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
import java.util.stream.Collectors;

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
    private final Collection<GroupBy_Line> groupByLines;

    private ExceptionRecordReport(String workshopId, long startDateTime, long endDateTime, Flux<Document> exceptionRecord$) {
        this.workshopId = workshopId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        groupByLines = exceptionRecord$.reduce(Maps.<String, GroupBy_Line>newConcurrentMap(), (acc, cur) -> {
            final String lineMachineId = cur.getString("lineMachine");
            final Document lineMachine = QueryService.findFromCache(LineMachine.class, lineMachineId).get();
            final String lineId = lineMachine.getString("line");
            final Document line = QueryService.findFromCache(Line.class, lineId).get();
            if (Objects.equals(workshopId, line.getString("workshop"))) {
                acc.compute(lineId, (k, v) -> Optional.ofNullable(v).orElse(new GroupBy_Line(line)).collect(cur));
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
        J.emptyIfNull(groupByLines).stream()
                .map(GroupBy_Line::toJsonNode)
                .forEach(arrayNode::add);
        return arrayNode;
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class GroupBy_Line {
        @EqualsAndHashCode.Include
        private final Line line = new Line();
        private final Map<String, GroupBy_Batch> batchMap = Maps.newConcurrentMap();

        private GroupBy_Line(Document line) {
            this.line.setId(line.getString(ID_COL));
            this.line.setName(line.getString("name"));
        }

        private GroupBy_Line collect(Document exceptionRecord) {
            final String silkId = exceptionRecord.getString("silk");
            if (J.nonBlank(silkId)) {
                final String batchId = QueryService.find(Silk.class, silkId).map(it -> it.getString("batch")).block();
                batchMap.compute(batchId, (k, v) -> Optional.ofNullable(v)
                        .orElse(new GroupBy_Batch(k))
                        .collect(exceptionRecord)
                );
            }
            return this;
        }

        private JsonNode toJsonNode() {
            final Collection<JsonNode> groupByBatch = batchMap.values().stream()
                    .map(GroupBy_Batch::toJsonNode)
                    .collect(Collectors.toList());
            final Map<String, Object> map = Map.of("line", line, "groupByBatch", groupByBatch);
            return MAPPER.convertValue(map, JsonNode.class);
        }
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class GroupBy_Batch {
        @EqualsAndHashCode.Include
        private final Batch batch = new Batch();
        private final Map<String, GroupBy_SilkException> silkExceptionMap = Maps.newConcurrentMap();

        private GroupBy_Batch(String batchId) {
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

        private GroupBy_Batch collect(Document exceptionRecord) {
            final String exceptionId = exceptionRecord.getString("exception");
            silkExceptionMap.compute(exceptionId, (k, v) -> Optional.ofNullable(v)
                    .orElse(new GroupBy_SilkException(k))
                    .collect(exceptionRecord)
            );
            return this;
        }

        private JsonNode toJsonNode() {
            final Collection<JsonNode> groupBySilkException = silkExceptionMap.values().stream()
                    .map(GroupBy_SilkException::toJsonNode)
                    .collect(Collectors.toList());
            final Map<String, Object> map = Map.of("batch", this.batch, "groupBySilkException", groupBySilkException);
            return MAPPER.convertValue(map, JsonNode.class);
        }
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class GroupBy_SilkException {
        @EqualsAndHashCode.Include
        private final SilkException silkException = new SilkException();
        private int silkCount = 0;

        private GroupBy_SilkException(String exceptionId) {
            final Document silkException = QueryService.findFromCache(SilkException.class, exceptionId).get();
            this.silkException.setId(silkException.getString(ID_COL));
            this.silkException.setName(silkException.getString("name"));
        }

        private GroupBy_SilkException collect(Document exceptionRecord) {
            silkCount++;
            return this;
        }

        private JsonNode toJsonNode() {
            final Map<String, Object> map = Map.of("silkException", silkException, "silkCount", silkCount);
            return MAPPER.convertValue(map, JsonNode.class);
        }
    }
}
