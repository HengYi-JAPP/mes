package com.hengyi.japp.mes.auto;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.application.report.StatisticsReport;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.Line;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jzb 2019-01-21
 */
public class DiffUtil {
    public static Stream<StatisticsReport.Item> diff(Batch batch, Grade grade, final Integer SILK_COUNT, final BigDecimal SILK_WEIGHT, Map<Line, Map<Grade, Integer>> LINE_MAP) {
        final Map<Line, Integer> diffLineMap = LINE_MAP.entrySet().parallelStream().map(entry -> {
            final Line line = entry.getKey();
            final Map<Grade, Integer> gradeMap = J.emptyIfNull(entry.getValue());
            final Integer silkCount = gradeMap.getOrDefault(grade, 0);
            return Pair.of(line, silkCount);
        }).filter(pair -> pair.getRight() > 0).collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        final int lineCount = diffLineMap.size();
        if (lineCount == 1) {
            return oneStream(batch, grade, SILK_COUNT, SILK_WEIGHT, diffLineMap);
        }
        if (lineCount == 0) {
            // 总数量分
            final String join = String.join("-", batch.getBatchNo(), grade.getName(), "全部为补充唛头，无法分配，尝试按总量分！");
//            System.out.println(join);

            final Map<Line, Integer> sumDiffLineMap = LINE_MAP.entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                final Map<Grade, Integer> gradeMap = J.emptyIfNull(entry.getValue());
                return gradeMap.values().parallelStream().mapToInt(it -> it).sum();
            }));
            return subDiff(batch, grade, SILK_COUNT, SILK_WEIGHT, sumDiffLineMap);
        }
        return subDiff(batch, grade, SILK_COUNT, SILK_WEIGHT, diffLineMap);
    }

    private static Stream<StatisticsReport.Item> subDiff(Batch batch, Grade grade, final Integer SILK_COUNT, final BigDecimal SILK_WEIGHT, Map<Line, Integer> diffLineMap) {
        final int lineCount = diffLineMap.size();
        if (lineCount == 1) {
            return oneStream(batch, grade, SILK_COUNT, SILK_WEIGHT, diffLineMap);
        }
        if (lineCount == 0) {
            // todo 按生产计划，机台分
            final String join = String.join("-", batch.getBatchNo(), grade.getName(), "全部为补充唛头，无法分配，按总量也无法分配，按生产计划，机台分！");
            System.out.println(join);
            return Stream.empty();
        }

        final Collection<StatisticsReport.Item> items = subDiffSilkCount(batch, grade, SILK_COUNT, diffLineMap);
        subDiffSilkWeight(batch, grade, SILK_WEIGHT, items);
        return items.stream();
    }

    private static void subDiffSilkWeight(Batch batch, Grade grade, final BigDecimal SILK_WEIGHT, Collection<StatisticsReport.Item> items) {
        if (grade.getSortBy() >= 100) {
            items.parallelStream().forEach(item -> {
                final BigDecimal silkCount = BigDecimal.valueOf(item.getSilkCount());
                final BigDecimal batchWeight = BigDecimal.valueOf(batch.getSilkWeight());
                final BigDecimal multiply = silkCount.multiply(batchWeight);
                item.setSilkWeight(multiply);
            });
            return;
        }
        final BigDecimal sumSilkCount = BigDecimal.valueOf(items.parallelStream().mapToInt(StatisticsReport.Item::getSilkCount).sum());
        items.parallelStream().forEach(item -> {
            final BigDecimal silkCount = BigDecimal.valueOf(item.getSilkCount());
            final BigDecimal silkWeight = SILK_WEIGHT.multiply(silkCount).divide(sumSilkCount, 3, RoundingMode.HALF_UP);
            item.setSilkWeight(silkWeight);
        });
        // 重量尾差处理
        endDiffSilkWeight(SILK_WEIGHT, items);
    }

    private static void endDiffSilkWeight(final BigDecimal SILK_WEIGHT, Collection<StatisticsReport.Item> items) {
        final BigDecimal sum = items.parallelStream()
                .map(StatisticsReport.Item::getSilkWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.equals(SILK_WEIGHT)) {
            return;
        }
        final BigDecimal endDiff = SILK_WEIGHT.subtract(sum);
        final StatisticsReport.Item item = items.parallelStream().max(Comparator.comparing(StatisticsReport.Item::getSilkWeight)).get();
        final BigDecimal silkWeight = item.getSilkWeight().add(endDiff);
        item.setSilkWeight(silkWeight);
    }

    private static Collection<StatisticsReport.Item> subDiffSilkCount(Batch batch, Grade grade, final Integer SILK_COUNT, Map<Line, Integer> diffLineMap) {
        final int sumSilkCount = diffLineMap.values().parallelStream().mapToInt(it -> it).sum();
        final Collection<StatisticsReport.Item> result = diffLineMap.entrySet().parallelStream().map(entry -> {
            final Line line = entry.getKey();
            final Integer silkCount = entry.getValue();
            final int count = SILK_COUNT * silkCount / sumSilkCount;
            final StatisticsReport.Item item = new StatisticsReport.Item(line, batch, grade);
            item.setSilkCount(count);
            return item;
        }).collect(Collectors.toList());
        // 颗数尾差处理
        endDiffSilkCount(SILK_COUNT, result);
        return result;
    }

    private static void endDiffSilkCount(final Integer SILK_COUNT, Collection<StatisticsReport.Item> items) {
        final int sum = items.parallelStream().mapToInt(StatisticsReport.Item::getSilkCount).sum();
        if (sum == SILK_COUNT) {
            return;
        }
        final int endDiff = SILK_COUNT - sum;
        final StatisticsReport.Item item = items.parallelStream().max(Comparator.comparing(StatisticsReport.Item::getSilkCount)).get();
        final int silkCount = item.getSilkCount() + endDiff;
        item.setSilkCount(silkCount);
    }

    private static Stream<StatisticsReport.Item> oneStream(Batch batch, Grade grade, final Integer SILK_COUNT, final BigDecimal SILK_WEIGHT, Map<Line, Integer> diffLineMap) {
        return diffLineMap.keySet().parallelStream().map(line -> {
            final StatisticsReport.Item item = new StatisticsReport.Item(line, batch, grade);
            item.setSilkCount(SILK_COUNT);
            item.setSilkWeight(SILK_WEIGHT);
            return item;
        });
    }
}
