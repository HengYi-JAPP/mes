package com.hengyi.japp.mes.auto.report.application.dto.statistic;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * @author jzb 2019-05-30
 */
@Slf4j
public class StatisticReportDiff_Batch {
    @Getter
    private final boolean bigSilkCar;
    private final BatchDTO batch;
    @Getter
    private final Collection<PackageBoxDTO> packageBoxes;
    @Getter
    private final int silkCount;
    @Getter
    private final BigDecimal silkWeight;
    // 各等级， 多少颗， 多少重
    private final Collection<GroupByGrade> groupByGrades;
    // 已知的线别  各等级  多少颗
    private final Collection<GroupByLine> groupByLines;
    private final int lineCount;
    @Getter(lazy = true)
    private final Collection<Item> reportItems = diff().collect(toList());

    public StatisticReportDiff_Batch(boolean bigSilkCar, BatchDTO batch, List<PackageBoxDTO> packageBoxes) {
        this.bigSilkCar = bigSilkCar;
        this.batch = batch;
        this.packageBoxes = J.emptyIfNull(packageBoxes);
        groupByGrades = this.packageBoxes.parallelStream()
                .collect(groupingBy(PackageBoxDTO::getGrade))
                .entrySet().parallelStream()
                .map(entry -> new GroupByGrade(entry.getKey(), entry.getValue()))
                .collect(toList());
        silkCount = groupByGrades.parallelStream()
                .mapToInt(GroupByGrade::getSilkCount)
                .sum();
        silkWeight = groupByGrades.parallelStream()
                .map(GroupByGrade::getSilkWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        groupByLines = this.packageBoxes.parallelStream()
                .flatMap(it -> J.emptyIfNull(it.getSilks()).parallelStream())
                .collect(groupingBy(it -> it.getLineMachine().getLine()))
                .entrySet().parallelStream()
                .map(entry -> new GroupByLine(entry.getKey(), entry.getValue()))
                .collect(toList());
        lineCount = groupByLines.size();
    }

    private Stream<Item> diff() {
        if (lineCount < 1) {
            return Stream.empty();
        }
        if (lineCount == 1) {
            final GroupByLine groupByLine = IterableUtils.get(groupByLines, 0);
            return groupByGrades.parallelStream().map(groupByGrade -> {
                final Item item = new Item(bigSilkCar, groupByLine.getLine(), batch, groupByGrade.getGrade());
                item.setSilkCount(groupByGrade.getSilkCount());
                item.setSilkWeight(groupByGrade.getSilkWeight());
                return item;
            });
        }
        return groupByGrades.parallelStream().flatMap(this::diff);
    }

    private Stream<Item> diff(GroupByGrade groupByGrade) {
        final GradeDTO grade = groupByGrade.getGrade();
        final int SILK_COUNT = groupByGrade.getSilkCount();
        final BigDecimal SILK_WEIGHT = groupByGrade.getSilkWeight();
        final Map<LineDTO, Map<GradeDTO, Integer>> LINE_MAP = groupByLines.parallelStream()
                .collect(toMap(GroupByLine::getLine, it ->
                        it.gradeMap.entrySet().parallelStream().collect(toMap(Map.Entry::getKey, _it -> _it.getValue().size()))
                ));
        return DiffUtil.diff(bigSilkCar, batch, grade, SILK_COUNT, SILK_WEIGHT, LINE_MAP);
    }

    public Collection<PackageBoxDTO> getUnDiffPackageBoxes() {
        if (lineCount < 1) {
            return packageBoxes;
        }
        return Collections.EMPTY_LIST;
    }

    private static class GroupByGrade {
        @Getter
        private final GradeDTO grade;
        @Getter
        private final Collection<PackageBoxDTO> packageBoxes;
        @Getter
        private final int silkCount;
        @Getter
        private final BigDecimal silkWeight;

        public GroupByGrade(GradeDTO grade, Collection<PackageBoxDTO> packageBoxes) {
            this.grade = grade;
            this.packageBoxes = packageBoxes;
            silkCount = packageBoxes.parallelStream()
                    .collect(summingInt(PackageBoxDTO::getSilkCount));
            silkWeight = packageBoxes.parallelStream()
                    .map(PackageBoxDTO::getNetWeight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    private static class GroupByLine {
        @Getter
        private final LineDTO line;
        private final Collection<SilkDTO> silks;
        private final Map<GradeDTO, List<SilkDTO>> gradeMap;

        public GroupByLine(LineDTO line, Collection<SilkDTO> silks) {
            this.line = line;
            this.silks = J.emptyIfNull(silks);
            gradeMap = this.silks.parallelStream().collect(groupingBy(SilkDTO::getGrade));
        }

        private Collection<SilkDTO> get(GradeDTO grade) {
            return gradeMap.getOrDefault(grade, Collections.EMPTY_LIST);
        }
    }

}
