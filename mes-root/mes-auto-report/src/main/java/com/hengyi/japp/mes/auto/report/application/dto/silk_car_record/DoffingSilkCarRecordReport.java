package com.hengyi.japp.mes.auto.report.application.dto.silk_car_record;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarRecordAggregateType;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.partitioningBy;

/**
 * @author jzb 2019-07-11
 */
@Slf4j
@Data
public class DoffingSilkCarRecordReport implements Serializable {
    private final String workshopId;
    private final long startDateTime;
    private final long endDateTime;
    @Getter
    private final Collection<GroupBy_Batch_Grade> groupByBatchGrades;

    private DoffingSilkCarRecordReport(String workshopId, long startDateTime, long endDateTime, Flux<SilkCarRecordAggregate> silkCarRecordAggregate$) {
        this.workshopId = workshopId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        groupByBatchGrades = silkCarRecordAggregate$
                .filter(it -> Objects.equals(DoffingType.AUTO, it.getDoffingType()) || Objects.equals(DoffingType.MANUAL, it.getDoffingType()))
                .reduce(Maps.<Pair<String, String>, GroupBy_Batch_Grade>newConcurrentMap(), (acc, cur) -> {
                    final Document batch = cur.getBatch();
                    final String batchId = batch.getString(ID_COL);
                    final Document grade = cur.getGrade();
                    final String gradeId = grade.getString(ID_COL);
                    final Pair<String, String> key = Pair.of(batchId, gradeId);
                    acc.compute(key, (k, v) -> ofNullable(v).orElse(new GroupBy_Batch_Grade(batch, grade)).collect(cur));
                    return acc;
                }).map(Map::values).block();
    }

    public static DoffingSilkCarRecordReport create(String workshopId, long startDateTime, long endDateTime) {
        final QueryService queryService = INJECTOR.getInstance(QueryService.class);
        final Collection<String> silkCarRecordIds = queryService.querySilkCarRecordIds(workshopId, startDateTime, endDateTime);
        final Flux<SilkCarRecordAggregate> silkCarRecordAggregate$ = Flux.fromIterable(J.emptyIfNull(silkCarRecordIds)).flatMap(SilkCarRecordAggregate::from);
        return new DoffingSilkCarRecordReport(workshopId, startDateTime, endDateTime, silkCarRecordAggregate$);
    }

    @SneakyThrows
    public ArrayNode toJsonNode() {
        final ArrayNode arrayNode = MAPPER.createArrayNode();
        groupByBatchGrades.forEach(it -> arrayNode.add(it.toJsonNode()));
        return arrayNode;
    }

    @Data
    public static class GroupBy_Batch_Grade {
        private final Document batch;
        private final Document grade;
        private final Collection<Item> items = Lists.newArrayList();

        public GroupBy_Batch_Grade(Document batch, Document grade) {
            this.batch = batch;
            this.grade = grade;
        }

//        public GroupBy_Batch_Grade(Document batch, Document grade, Collection<SilkCarRecordAggregate> silkCarRecordAggregates) {
//            this.batch = batch;
//            this.grade = grade;
//            items = silkCarRecordAggregates.parallelStream().map(it -> new Item(batch, grade, it)).collect(toList());
//        }

        @SneakyThrows
        public ObjectNode toJsonNode() {
            final ObjectNode objectNode = MAPPER.createObjectNode();
            objectNode.set("batch", MAPPER.readTree(batch.toJson()));
            objectNode.set("grade", MAPPER.readTree(grade.toJson()));
            final ArrayNode itemsArrayNode = MAPPER.createArrayNode();
            objectNode.set("items", itemsArrayNode);
            items.forEach(item -> itemsArrayNode.add(item.toJsonNode()));
            return objectNode;
        }

        public GroupBy_Batch_Grade collect(SilkCarRecordAggregate silkCarRecordAggregate) {
            final Item item = new Item(batch, grade, silkCarRecordAggregate);
            items.add(item);
            return this;
        }
    }

    @Data
    public static class Item {
        private final String id;
        private final Document batch;
        private final Document grade;
        private final Document silkCar;
        private final Document creator;
        private final Collection<EventSource.DTO> eventSources;
        private final SilkCarRecordAggregateType type;
        private final int silkCount;
        private final BigDecimal netWeight;
        // 是否已经称重
        private final boolean hasNetWeight;

        public Item(Document batch, Document grade, SilkCarRecordAggregate silkCarRecordAggregate) {
            this.id = silkCarRecordAggregate.getId();
            this.silkCar = silkCarRecordAggregate.getSilkCar();
            this.creator = silkCarRecordAggregate.getCreator();
            this.eventSources = silkCarRecordAggregate.getEventSourceDtos();
            this.batch = batch;
            this.grade = grade;
            this.type = silkCarRecordAggregate.getType();
            silkCount = silkCarRecordAggregate.getInitSilkRuntimeDtos().size();
            if (grade.getInteger("sortBy") >= 100) {
                hasNetWeight = true;
                netWeight = BigDecimal.valueOf(batch.getDouble("silkWeight")).multiply(BigDecimal.valueOf(silkCount));
            } else {
                final Map<Boolean, List<Document>> weightSilkMap = Flux.fromIterable(silkCarRecordAggregate.getInitSilkRuntimeDtos())
                        .flatMap(it -> QueryService.find(Silk.class, it.getSilk())).toStream()
                        .collect(partitioningBy(it -> {
                            final Double weight = it.getDouble("weight");
                            return weight != null && weight > 0;
                        }));
                hasNetWeight = J.isEmpty(weightSilkMap.get(false));
                if (hasNetWeight) {
                    final Collection<Document> weightSilks = J.emptyIfNull(weightSilkMap.get(true));
                    netWeight = weightSilks.parallelStream().map(it -> {
                        final Double weight = it.getDouble("weight");
                        return BigDecimal.valueOf(weight);
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
                } else {
                    // 没称重的先按锭重计算
                    netWeight = BigDecimal.valueOf(batch.getDouble("silkWeight")).multiply(BigDecimal.valueOf(silkCount));
                }
            }
        }

        @SneakyThrows
        public ObjectNode toJsonNode() {
            final ObjectNode objectNode = MAPPER.createObjectNode()
                    .put("type", type.name())
                    .put("id", id)
                    .put("hasNetWeight", hasNetWeight)
                    .put("silkCount", silkCount)
                    .put("netWeight", netWeight);
            objectNode.set("silkCar", MAPPER.readTree(silkCar.toJson()));
            objectNode.set("creator", MAPPER.readTree(creator.toJson()));
            final ArrayNode eventSourcesArrayNode = MAPPER.createArrayNode();
            objectNode.set("eventSources", eventSourcesArrayNode);
            J.emptyIfNull(eventSources).stream()
                    .map(SilkCarRecordAggregate::toJsonNode)
                    .forEach(eventSourcesArrayNode::add);
            return objectNode;
        }
    }

}
