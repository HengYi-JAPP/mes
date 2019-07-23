package com.hengyi.japp.mes.auto.report.application.dto.statistic;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.BatchDTO;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.GradeDTO;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.Item;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.LineDTO;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toMap;

/**
 * @author jzb 2019-01-21
 */
public class DiffUtil {
    public static Stream<Item> diff(boolean bigSilkCar, BatchDTO batch, GradeDTO grade, final Integer SILK_COUNT, final BigDecimal SILK_WEIGHT, Map<LineDTO, Map<GradeDTO, Integer>> LINE_MAP) {
        final Map<LineDTO, Integer> diffLineMap = LINE_MAP.entrySet().parallelStream().map(entry -> {
            final LineDTO line = entry.getKey();
            final Map<GradeDTO, Integer> gradeMap = J.emptyIfNull(entry.getValue());
            final Integer silkCount = gradeMap.getOrDefault(grade, 0);
            return Pair.of(line, silkCount);
        }).filter(pair -> pair.getRight() > 0).collect(toMap(Pair::getKey, Pair::getValue));

        final int lineCount = diffLineMap.size();
        if (lineCount == 1) {
            return oneStream(bigSilkCar, batch, grade, SILK_COUNT, SILK_WEIGHT, diffLineMap);
        }
        if (lineCount == 0) {
            // 总数量分
            final String join = String.join("-", batch.getBatchNo(), grade.getName(), "全部为补充唛头，无法分配，尝试按总量分！");
            System.out.println(join);

            final Map<LineDTO, Integer> sumDiffLineMap = LINE_MAP.entrySet().parallelStream().collect(toMap(Map.Entry::getKey, entry -> {
                final Map<GradeDTO, Integer> gradeMap = J.emptyIfNull(entry.getValue());
                return gradeMap.values().parallelStream().mapToInt(it -> it).sum();
            }));
            return subDiff(bigSilkCar, batch, grade, SILK_COUNT, SILK_WEIGHT, sumDiffLineMap);
        }
        return subDiff(bigSilkCar, batch, grade, SILK_COUNT, SILK_WEIGHT, diffLineMap);
    }

    private static Stream<Item> subDiff(boolean bigSilkCar, BatchDTO batch, GradeDTO grade, final Integer SILK_COUNT, final BigDecimal SILK_WEIGHT, Map<LineDTO, Integer> diffLineMap) {
        final int lineCount = diffLineMap.size();
        if (lineCount == 1) {
            return oneStream(bigSilkCar, batch, grade, SILK_COUNT, SILK_WEIGHT, diffLineMap);
        }
        if (lineCount == 0) {
            // todo 按生产计划，机台分
            final String join = String.join("-", batch.getBatchNo(), grade.getName(), "全部为补充唛头，无法分配，按总量也无法分配，按生产计划，机台分！");
            System.out.println(join);
            return Stream.empty();
        }

        final Collection<Item> items = subDiffSilkCount(bigSilkCar, batch, grade, SILK_COUNT, diffLineMap);
        subDiffSilkWeight(batch, grade, SILK_WEIGHT, items);
        return items.stream();
    }

    private static void subDiffSilkWeight(BatchDTO batch, GradeDTO grade, final BigDecimal SILK_WEIGHT, Collection<Item> items) {
        if (grade.getSortBy() >= 100) {
            items.parallelStream().forEach(item -> {
                final BigDecimal silkCount = BigDecimal.valueOf(item.getSilkCount());
                final BigDecimal batchWeight = batch.getSilkWeight();
                final BigDecimal multiply = silkCount.multiply(batchWeight);
                item.setSilkWeight(multiply);
            });
            return;
        }
        final BigDecimal sumSilkCount = BigDecimal.valueOf(items.parallelStream().mapToInt(Item::getSilkCount).sum());
        items.parallelStream().forEach(item -> {
            final BigDecimal silkCount = BigDecimal.valueOf(item.getSilkCount());
            final BigDecimal silkWeight = SILK_WEIGHT.multiply(silkCount).divide(sumSilkCount, 3, RoundingMode.HALF_UP);
            item.setSilkWeight(silkWeight);
        });
        endDiffSilkWeight(SILK_WEIGHT, items);
    }

    /**
     * 重量尾差处理
     */
    private static void endDiffSilkWeight(final BigDecimal SILK_WEIGHT, Collection<Item> items) {
        final BigDecimal sum = items.parallelStream()
                .map(Item::getSilkWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.equals(SILK_WEIGHT)) {
            return;
        }
        final BigDecimal endDiff = SILK_WEIGHT.subtract(sum);
        final Item item = items.parallelStream().max(comparing(Item::getSilkWeight)).get();
        final BigDecimal silkWeight = item.getSilkWeight().add(endDiff);
        item.setSilkWeight(silkWeight);
    }

    private static Collection<Item> subDiffSilkCount(boolean bigSilkCar, BatchDTO batch, GradeDTO grade, final Integer SILK_COUNT, Map<LineDTO, Integer> diffLineMap) {
        final int sumSilkCount = diffLineMap.values().parallelStream().mapToInt(it -> it).sum();
        final Collection<Item> result = diffLineMap.entrySet().parallelStream().map(entry -> {
            final Item item = new Item(bigSilkCar, entry.getKey(), batch, grade);
            final Integer silkCount = entry.getValue();
            final int count = SILK_COUNT * silkCount / sumSilkCount;
            item.setSilkCount(count);
            return item;
        }).collect(Collectors.toList());
        endDiffSilkCount(SILK_COUNT, result);
        return result;
    }

    /**
     * 颗数尾差处理
     */
    private static void endDiffSilkCount(final Integer SILK_COUNT, Collection<Item> items) {
        final int sum = items.parallelStream().collect(summingInt(Item::getSilkCount));
        if (sum == SILK_COUNT) {
            return;
        }
        final int endDiff = SILK_COUNT - sum;
        final Item item = items.parallelStream().max(comparing(Item::getSilkCount)).get();
        final int silkCount = item.getSilkCount() + endDiff;
        item.setSilkCount(silkCount);
    }

    private static Stream<Item> oneStream(boolean bigSilkCar, BatchDTO batch, GradeDTO grade, final Integer SILK_COUNT, final BigDecimal SILK_WEIGHT, Map<LineDTO, Integer> diffLineMap) {
        return diffLineMap.keySet().parallelStream().map(line -> {
            final Item item = new Item(bigSilkCar, line, batch, grade);
            item.setSilkCount(SILK_COUNT);
            item.setSilkWeight(SILK_WEIGHT);
            return item;
        });
    }
}
