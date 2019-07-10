package com.hengyi.japp.mes.auto.report.application.dto.statistic;

import com.google.common.collect.ComparisonChain;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.BatchDTO;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.GradeDTO;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.Item;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.LineDTO;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-07-06
 */
@Data
public class XlsxItem implements Comparable<XlsxItem>, Serializable {
    private final LineDTO line;
    private final Collection<XlsxItemRowByBatch> batchData;

    public static Collection<XlsxItem> collect(Collection<Item> originalItems) {
        final List<XlsxItem> result = aaCollectStream(originalItems).parallel()
                .collect(groupingBy(Item::getLine))
                .entrySet().parallelStream()
                .map(entry -> {
                    final LineDTO line = entry.getKey();
                    final List<XlsxItemRowByBatch> batchData = batchCollectStream(entry.getValue()).collect(toList());
                    Collections.sort(batchData);
                    return new XlsxItem(line, batchData);
                })
                .collect(toList());
        Collections.sort(result);
        return result;
    }

    // 报表体现需要把 AAA 加到 AA 中
    private static Stream<Item> aaCollectStream(Collection<Item> originalItems) {
        final var aaCollect = originalItems.parallelStream().filter(it -> {
            final GradeDTO grade = it.getGrade();
            return grade.getSortBy() >= 100;
        }).collect(groupingBy(it -> Triple.of(it.isBigSilkCar(), it.getLine(), it.getBatch())));
        final GradeDTO aaGrade = aaCollect.values().parallelStream()
                .flatMap(Collection::parallelStream)
                .map(Item::getGrade)
                .filter(it -> "AA".equals(it.getName()))
                .findAny()
                .orElse(null);
        if (aaGrade == null) {
            return originalItems.parallelStream();
        }
        final var aaStream = aaCollect.entrySet().parallelStream().map(entry -> {
            final Triple<Boolean, LineDTO, BatchDTO> triple = entry.getKey();
            final boolean isBigSilkCar = triple.getLeft();
            final LineDTO line = triple.getMiddle();
            final BatchDTO batch = triple.getRight();
            final Item item = new Item(isBigSilkCar, line, batch, aaGrade);
            final int silkCount = entry.getValue().parallelStream().mapToInt(Item::getSilkCount).sum();
            item.setSilkCount(silkCount);
            final BigDecimal silkWeight = entry.getValue().parallelStream().map(Item::getSilkWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setSilkWeight(silkWeight);
            return item;
        });
        final var abcStream = originalItems.parallelStream().filter(it -> {
            final GradeDTO grade = it.getGrade();
            return grade.getSortBy() < 100;
        });
        return Stream.concat(abcStream, aaStream).parallel();
    }

    private static Stream<XlsxItemRowByBatch> batchCollectStream(List<Item> items) {
        return items.parallelStream()
                .collect(groupingBy(it -> Pair.of(it.isBigSilkCar(), it.getBatch())))
                .entrySet().parallelStream()
                .map(entry -> {
                    final Pair<Boolean, BatchDTO> pair = entry.getKey();
                    final boolean bigSilkCar = pair.getLeft();
                    final BatchDTO batch = pair.getRight();
                    final List<XlsxItemRowByBatch_Grade> gradeData = entry.getValue().parallelStream()
                            .map(item -> {
                                final GradeDTO grade = item.getGrade();
                                final int silkCount = item.getSilkCount();
                                final BigDecimal silkWeight = item.getSilkWeight();
                                return new XlsxItemRowByBatch_Grade(grade, silkCount, silkWeight);
                            })
                            .collect(toList());
                    return new XlsxItemRowByBatch(bigSilkCar, batch, gradeData);
                });
    }

    @Override
    public int compareTo(XlsxItem o) {
        return ComparisonChain.start()
                .compare(line.getName(), o.line.getName())
                .result();
    }

    @Data
    public static class XlsxItemRowByBatch implements Comparable<XlsxItemRowByBatch>, Serializable {
        private final boolean bigSilkCar;
        private final BatchDTO batch;
        private final Collection<XlsxItemRowByBatch_Grade> gradeData;

        public String getSpec() {
            final String spec = batch.getSpec();
            if (bigSilkCar) {
                return spec + "（车丝）";
            }
            return spec;
        }

        @Override
        public int compareTo(XlsxItemRowByBatch o) {
            return ComparisonChain.start()
                    .compareFalseFirst(bigSilkCar, o.bigSilkCar)
                    .compare(batch.getBatchNo(), o.batch.getBatchNo())
                    .result();
        }
    }

    @Data
    public static class XlsxItemRowByBatch_Grade implements Serializable {
        private final GradeDTO grade;
        private final int silkCount;
        private final BigDecimal silkWeight;
    }
}
