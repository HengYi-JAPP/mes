package com.hengyi.japp.mes.auto.report.application.dto.silk_car_record;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-07-11
 */
@Slf4j
@Data
public class DoffingSilkCarRecordReportNew implements Serializable {
    private final Collection<GroupBy_BatchGrade> groupByBatchGrades;

    public DoffingSilkCarRecordReportNew(Collection<String> silkCarRecordIds) {
        groupByBatchGrades = Flux.fromIterable(J.emptyIfNull(silkCarRecordIds))
                .flatMap(SilkCarRecordAggregate::from)
                .filter(it -> Objects.nonNull(it.getDoffingDateTime()))
                .reduce(Maps.<Pair<String, String>, GroupBy_BatchGrade>newConcurrentMap(), (acc, cur) -> {
                    final String batchId = cur.getBatch().getString(ID_COL);
                    final String gradeId = cur.getGrade().getString(ID_COL);
                    acc.compute(Pair.of(batchId, gradeId), (k, v) -> Optional.ofNullable(v).orElse(new GroupBy_BatchGrade(batchId, gradeId)).collect(cur));
                    return acc;
                }).map(Map::values).block();
    }

    @SneakyThrows
    public JsonNode toJsonNode() {
        final List list = groupByBatchGrades.parallelStream().map(GroupBy_BatchGrade::toJsonNode).collect(toList());
        return MAPPER.convertValue(list, JsonNode.class);
    }

    @Data
    public static class GroupBy_BatchGrade {
        private final Document batch;
        // 批号锭重
        private final BigDecimal silkWeight;
        private final Document grade;
        private final Metrics totalMetrics = new Metrics();
        private final Metrics toDtyMetrics = new Metrics();
        private final Metrics toDtyConfirmMetrics = new Metrics();
        private final Metrics nonWeightMetrics = new Metrics();

        public GroupBy_BatchGrade(String batchId, String gradeId) {
            this.batch = QueryService.findFromCache(Batch.class, batchId).get();
            this.grade = QueryService.findFromCache(Grade.class, gradeId).get();
            silkWeight = BigDecimal.valueOf(batch.getDouble("silkWeight"));
        }

        @SneakyThrows
        public JsonNode toJsonNode() {
            final Map map = Map.of(
                    "batch", MAPPER.readTree(this.batch.toJson()),
                    "grade", MAPPER.readTree(grade.toJson()),
                    "toDtyMetrics", toDtyMetrics,
                    "toDtyConfirmMetrics", toDtyConfirmMetrics,
                    "nonWeightMetrics", nonWeightMetrics,
                    "totalMetrics", totalMetrics
            );
            return MAPPER.convertValue(map, JsonNode.class);
        }

        public GroupBy_BatchGrade collect(SilkCarRecordAggregate silkCarRecordAggregate) {
            final CalcData calcData = calc(silkCarRecordAggregate);
            totalMetrics.collect(calcData);
            if (calcData.hasToDty) {
                toDtyMetrics.collect(calcData);
            }
            if (calcData.hasToDtyConfirm) {
                toDtyConfirmMetrics.collect(calcData);
            }
            if (!calcData.hasNetWeight) {
                nonWeightMetrics.collect(calcData);
            }
            return this;
        }

        // 没称重的先按锭重计算
        private CalcData calc(SilkCarRecordAggregate silkCarRecordAggregate) {
            final CalcData calcData = new CalcData(silkCarRecordAggregate.getInitSilkRuntimeDtos().size());
            final BigDecimal aaNetWeight = silkWeight.multiply(BigDecimal.valueOf(calcData.silkCount));
            calcData.hasNetWeight = true;
            calcData.netWeight = aaNetWeight;

            silkCarRecordAggregate.getEventSourceDtos().forEach(dto -> {
                if (EventSourceType.ToDtyEvent == dto.getType()) {
                    calcData.hasToDty = true;
                }
                if (EventSourceType.ToDtyConfirmEvent == dto.getType()) {
                    calcData.hasToDtyConfirm = true;
                }
            });

            if (grade.getInteger("sortBy") >= 100) {
                return calcData;
            }

            final Map<Boolean, List<Document>> weightSilkMap = Flux.fromIterable(silkCarRecordAggregate.getInitSilkRuntimeDtos())
                    .flatMap(it -> QueryService.find(Silk.class, it.getSilk())).toStream()
                    .collect(partitioningBy(it -> {
                        final Double weight = it.getDouble("weight");
                        return weight != null && weight > 0;
                    }));
            calcData.hasNetWeight = J.isEmpty(weightSilkMap.get(false));
            if (calcData.hasNetWeight) {
                final Collection<Document> weightSilks = J.emptyIfNull(weightSilkMap.get(true));
                calcData.netWeight = weightSilks.parallelStream().map(it -> {
                    final Double weight = it.getDouble("weight");
                    return BigDecimal.valueOf(weight);
                }).reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            return calcData;
        }
    }

    @Data
    public static class CalcData {
        private final int silkCount;
        private BigDecimal netWeight = BigDecimal.ZERO;
        private boolean hasToDty;
        private boolean hasToDtyConfirm;
        private boolean hasNetWeight = true;
    }

    @Data
    public static class Metrics {
        private int silkCarRecordCount;
        private int silkCount;
        private BigDecimal netWeight = BigDecimal.ZERO;

        private void collect(CalcData calcData) {
            silkCarRecordCount++;
            silkCount += calcData.silkCount;
            netWeight = netWeight.add(calcData.netWeight);
        }
    }

}
