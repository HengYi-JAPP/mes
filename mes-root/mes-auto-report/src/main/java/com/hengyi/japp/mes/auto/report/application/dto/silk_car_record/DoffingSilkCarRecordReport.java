package com.hengyi.japp.mes.auto.report.application.dto.silk_car_record;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import lombok.Data;
import lombok.SneakyThrows;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static java.util.stream.Collectors.*;

/**
 * @author jzb 2019-07-11
 */
@Data
public class DoffingSilkCarRecordReport implements Serializable {
    private final Collection<GroupBy_Batch_Grade> groupByBatchGrades;

    public DoffingSilkCarRecordReport(List<String> silkCarRecordIds) {
        groupByBatchGrades = Flux.fromIterable(J.emptyIfNull(silkCarRecordIds))
                .flatMap(SilkCarRecordAggregate::from)
                .filter(it -> Objects.nonNull(it.getDoffingDateTime())).toStream()
                .collect(groupingBy(it -> it.getBatch().getString(ID_COL)))
                .entrySet().parallelStream()
                .flatMap(entry -> {
                    final Document batch = QueryService.findFromCache(Batch.class, entry.getKey()).get();
                    return entry.getValue().parallelStream()
                            .collect(groupingBy(it -> it.getGrade().getString(ID_COL)))
                            .entrySet().parallelStream()
                            .map(entry2 -> {
                                final Document grade = QueryService.findFromCache(Grade.class, entry2.getKey()).get();
                                return new GroupBy_Batch_Grade(batch, grade, entry2.getValue());
                            });
                })
                .collect(toList());
    }

    @SneakyThrows
    public ArrayNode toJsonNode() {
        final ArrayNode arrayNode = MAPPER.createArrayNode();
        groupByBatchGrades.forEach(it -> arrayNode.add(it.toResponse()));
        return arrayNode;
    }

    @Data
    public static class GroupBy_Batch_Grade {
        private final Document batch;
        private final Document grade;
        private final Collection<Item> items;
        private final int silkCount;
        private final BigDecimal netWeight;

        public GroupBy_Batch_Grade(Document batch, Document grade, Collection<SilkCarRecordAggregate> silkCarRecordAggregates) {
            this.batch = batch;
            this.grade = grade;
            items = silkCarRecordAggregates.parallelStream().map(it -> new Item(batch, grade, it)).collect(toList());
            silkCount = items.parallelStream().collect(summingInt(Item::getSilkCount));
            netWeight = items.parallelStream().map(Item::getNetWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        @SneakyThrows
        public ObjectNode toResponse() {
            final ObjectNode objectNode = MAPPER.createObjectNode()
                    .put("silkCount", silkCount)
                    .put("netWeight", netWeight);
            objectNode.set("batch", MAPPER.readTree(batch.toJson()));
            objectNode.set("grade", MAPPER.readTree(grade.toJson()));
            final ArrayNode itemsArrayNode = MAPPER.createArrayNode();
            objectNode.set("items", itemsArrayNode);
            final ArrayNode noWeightItemsArrayNode = MAPPER.createArrayNode();
            objectNode.set("noWeightItems", noWeightItemsArrayNode);
            items.forEach(item -> {
                final SilkCarRecordAggregate silkCarRecordAggregate = item.getSilkCarRecordAggregate();
                final ObjectNode silkCarRecordAggregateNode = silkCarRecordAggregate.toResponse();
                itemsArrayNode.add(silkCarRecordAggregateNode);
                if (!item.hasNetWeight) {
                    noWeightItemsArrayNode.add(silkCarRecordAggregateNode);
                }
            });
            return objectNode;
        }
    }

    @Data
    public static class Item {
        private final Document batch;
        private final Document grade;
        private final SilkCarRecordAggregate silkCarRecordAggregate;
        private final int silkCount;
        private final BigDecimal netWeight;
        // 是否已经称重
        private final boolean hasNetWeight;
        private final Collection<Document> noWeightSilks;

        public Item(Document batch, Document grade, SilkCarRecordAggregate silkCarRecordAggregate) {
            this.batch = batch;
            this.grade = grade;
            this.silkCarRecordAggregate = silkCarRecordAggregate;
            silkCount = silkCarRecordAggregate.getInitSilkRuntimeDtos().size();
            if (grade.getInteger("sortBy") >= 100) {
                hasNetWeight = true;
                netWeight = BigDecimal.valueOf(batch.getDouble("silkWeight")).multiply(BigDecimal.valueOf(silkCount));
                noWeightSilks = Collections.EMPTY_LIST;
            } else {
                final Map<Boolean, List<Document>> weightSilkMap = Flux.fromIterable(silkCarRecordAggregate.getInitSilkRuntimeDtos())
                        .flatMap(it -> QueryService.find(Silk.class, it.getSilk())).toStream()
                        .collect(partitioningBy(it -> {
                            final Double weight = it.getDouble("weight");
                            return weight != null && weight > 0;
                        }));
                noWeightSilks = J.emptyIfNull(weightSilkMap.get(false));
                hasNetWeight = J.isEmpty(noWeightSilks);
                // 没称重的先按锭重计算
                final BigDecimal notWeightSum = BigDecimal.valueOf(batch.getDouble("silkWeight")).multiply(BigDecimal.valueOf(noWeightSilks.size()));
                final Collection<Document> weightSilks = J.emptyIfNull(weightSilkMap.get(true));
                final BigDecimal weightSum = weightSilks.parallelStream().map(it -> {
                    final Double weight = it.getDouble("weight");
                    return BigDecimal.valueOf(weight);
                }).reduce(BigDecimal.ZERO, BigDecimal::add);
                netWeight = notWeightSum.add(weightSum);
            }
        }
    }

}
