package com.hengyi.japp.mes.auto.application.report;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.hengyi.japp.mes.auto.domain.*;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 分线别算法
 *
 * @author jzb 2018-08-12
 */
@Data
public class StatisticsReportDayLineDiff implements Serializable {
    private final Batch batch;
    private final Collection<PackageBox> SUM_PACKAGE_BOX;
    // 每个等级，多少颗，多少重
    private final Map<Grade, Pair<Integer, BigDecimal>> SUM_GRADE_MAP = Maps.newConcurrentMap();
    private final Multimap<Line, Pair<Silk, Grade>> SUM_LINE_MAP = HashMultimap.create();
    private final Collection<PackageBox> UNKOWN_PACKAGE_BOX_LIST = Lists.newArrayList();
//    private final int SUM_SILK_COUNT;
//    private final BigDecimal SUM_NET_WEIGHT;

    public StatisticsReportDayLineDiff(Batch batch, Collection<PackageBox> packageBoxes) {
        this.batch = batch;
        SUM_PACKAGE_BOX = J.emptyIfNull(packageBoxes);
        SUM_PACKAGE_BOX.forEach(this::collect);
    }

    private void collect(PackageBox packageBox) {
        final Grade grade = packageBox.getGrade();
        final int silkCount = packageBox.getSilkCount();
        final BigDecimal netWeight = new BigDecimal(Double.toString(packageBox.getNetWeight()));
        SUM_GRADE_MAP.compute(grade, (k, v) -> {
            if (v == null) {
                return Pair.of(silkCount, netWeight);
            } else {
                return Pair.of(silkCount + v.getLeft(), netWeight.add(v.getRight()));
            }
        });
//        collectToLine(packageBox);
    }

    private void collectToLine(PackageBox packageBox) {
        final Grade grade = packageBox.getGrade();
        final Collection<Silk> silks = packageBox.getSilks();
        if (J.nonEmpty(silks)) {
            silks.forEach(silk -> {
                final LineMachine lineMachine = silk.getLineMachine();
                final Line line = lineMachine.getLine();
                SUM_LINE_MAP.put(line, Pair.of(silk, grade));
            });
            return;
        }
        final Collection<SilkCarRecord> silkCarRecords = packageBox.getSilkCarRecords();
        if (J.nonEmpty(silkCarRecords)) {
            silkCarRecords.stream()
                    .map(SilkCarRecord::initSilks)
                    .filter(J::nonEmpty)
                    .flatMap(Collection::stream)
                    .map(SilkRuntime::getSilk)
                    .forEach(silk -> {
                        final LineMachine lineMachine = silk.getLineMachine();
                        final Line line = lineMachine.getLine();
                        SUM_LINE_MAP.put(line, Pair.of(silk, grade));
                    });
            return;
        }
        UNKOWN_PACKAGE_BOX_LIST.add(packageBox);
    }

    public Stream<StatisticsReportDay.Item> itemStream() {
        return Stream.empty();
//        return SUM_LINE_MAP.keys().stream().flatMap(line -> {
//            return SUM_LINE_MAP.get(line).stream()
//                    .collect(Collectors.groupingBy(Pair::getRight))
//                    .entrySet().stream()
//                    .map(entry -> {
//                        final Grade grade = entry.getKey();
//                        final int silkCount = J.emptyIfNull(entry.getValue()).size();
//                        final StatisticsReportDay.Item item = new StatisticsReportDay.Item(line, batch, grade);
//                        item.setSilkCount(silkCount);
//                        item.setSilkWeight(BigDecimal.ZERO);
//                        return item;
//                    });
//        });
    }

    @Data
    public static class LineItem {
        private final Line line;
        private final Collection<Pair<Silk, Grade>> silkPairs;
    }
}
