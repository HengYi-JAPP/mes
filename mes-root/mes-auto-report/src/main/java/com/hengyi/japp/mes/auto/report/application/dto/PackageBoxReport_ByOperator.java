package com.hengyi.japp.mes.auto.report.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;

/**
 * @author jzb 2019-10-12
 */
public class PackageBoxReport_ByOperator {
    private final String workshopId;
    private final LocalDate startBudat;
    private final LocalDate endBudat;
    @Getter
    private final Collection<GroupBy_Operator> groupByOperators;

    public PackageBoxReport_ByOperator(String workshopId, LocalDate startBudat, LocalDate endBudat, Collection<String> packageBoxIds) {
        this.workshopId = workshopId;
        this.startBudat = startBudat;
        this.endBudat = endBudat;
        groupByOperators = Flux.fromIterable(J.emptyIfNull(packageBoxIds))
                .flatMap(it -> QueryService.find(PackageBox.class, it))
                .reduce(Maps.<String, GroupBy_Operator>newConcurrentMap(), (acc, cur) -> {
                    acc.compute(cur.getString("creator"), (k, v) -> Optional.ofNullable(v).orElse(new GroupBy_Operator(k)).collect(cur));
                    return acc;
                }).map(Map::values).block();
    }

    public static PackageBoxReport_ByOperator create(String workshopId, LocalDate startBudat, LocalDate endBudat) {
        final QueryService queryService = INJECTOR.getInstance(QueryService.class);
        final Collection<String> ids = queryService.queryPackageBoxIds(workshopId, startBudat, endBudat);
        return new PackageBoxReport_ByOperator(workshopId, startBudat, endBudat, ids);
    }

    public static PackageBoxReport_ByOperator create(String workshopId, long startDateTime, long endDateTime) {
        final QueryService queryService = INJECTOR.getInstance(QueryService.class);
        final Collection<String> ids = queryService.queryPackageBoxIds(workshopId, startDateTime, endDateTime);
        return new PackageBoxReport_ByOperator(workshopId, null, null, ids);
    }

    public JsonNode toJsonNode() {
        final ArrayNode arrayNode = MAPPER.createArrayNode();
        J.emptyIfNull(groupByOperators).stream().map(GroupBy_Operator::toJsonNode).forEach(arrayNode::add);
        return arrayNode;
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class GroupBy_Operator {
        @EqualsAndHashCode.Include
        private final Operator operator = new Operator();
        private final Map<Pair<String, String>, GroupBy_BatchGrade> batchGradeMap = Maps.newConcurrentMap();

        public GroupBy_Operator(String id) {
            final Document operator = QueryService.find(Operator.class, id).block();
            this.operator.setId(operator.getString(ID_COL));
            this.operator.setName(operator.getString("name"));
            this.operator.setHrId(operator.getString("hrId"));
        }

        public GroupBy_Operator collect(Document packageBoxe) {
            final String batchId = packageBoxe.getString("batch");
            final String gradeId = packageBoxe.getString("grade");
            batchGradeMap.compute(Pair.of(batchId, gradeId), (k, v) -> Optional.ofNullable(v).orElse(new GroupBy_BatchGrade(batchId, gradeId)).collect(packageBoxe));
            return this;
        }

        public JsonNode toJsonNode() {
            final Collection<GroupBy_Product> products = Flux.fromIterable(batchGradeMap.values())
                    .reduce(Maps.<Product, GroupBy_Product>newConcurrentMap(), (acc, cur) -> {
                        final Product product = cur.getBatch().getProduct();
                        acc.compute(product, (k, v) -> Optional.ofNullable(v).orElse(new GroupBy_Product(k)).collect(cur));
                        return acc;
                    }).map(Map::values).block();
            final Map<String, Object> map = Map.of("operator", this.operator, "products", products);
            return MAPPER.convertValue(map, JsonNode.class);
        }
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class GroupBy_BatchGrade {
        @EqualsAndHashCode.Include
        private final Batch batch = new Batch();
        @EqualsAndHashCode.Include
        private final Grade grade = new Grade();
        private int packageBoxCount = 0;
        private int silkCountSum = 0;
        private BigDecimal netWeightSum = BigDecimal.ZERO;

        public GroupBy_BatchGrade(String batchId, String gradeId) {
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

            final Document grade = QueryService.findFromCache(Grade.class, gradeId).get();
            this.grade.setId(grade.getString(ID_COL));
            this.grade.setName(grade.getString("name"));
            this.grade.setSortBy(grade.getInteger("sortBy"));
        }

        public GroupBy_BatchGrade collect(Document packageBoxe) {
            packageBoxCount++;
            silkCountSum += packageBoxe.getInteger("silkCount");
            final Double netWeight = packageBoxe.getDouble("netWeight");
            netWeightSum = netWeightSum.add(netWeight == null ? BigDecimal.ZERO : BigDecimal.valueOf(netWeight));
            return this;
        }
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class GroupBy_Product {
        @EqualsAndHashCode.Include
        private final Product product;
        private int packageBoxCount = 0;
        private int silkCountSum = 0;
        private BigDecimal netWeightSum = BigDecimal.ZERO;

        public GroupBy_Product collect(GroupBy_BatchGrade collect) {
            packageBoxCount += collect.packageBoxCount;
            silkCountSum += collect.silkCountSum;
            netWeightSum = netWeightSum.add(collect.netWeightSum);
            return this;
        }
    }
}
