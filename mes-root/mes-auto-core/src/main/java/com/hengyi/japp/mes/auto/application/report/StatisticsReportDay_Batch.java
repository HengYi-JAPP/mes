package com.hengyi.japp.mes.auto.application.report;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.DiffUtil;
import com.hengyi.japp.mes.auto.domain.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
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
@Slf4j
@Data
public class StatisticsReportDay_Batch implements Serializable {
    private final Batch batch;
    // 各等级， 多少颗， 多少重
    private final Map<Grade, Pair<Integer, BigDecimal>> GRADE_MAP = Maps.newConcurrentMap();
    // 已知的线别  各等级  多少颗
    private final Map<Line, Map<Grade, Integer>> LINE_MAP = Maps.newConcurrentMap();

    public StatisticsReportDay_Batch(Batch batch, Collection<PackageBox> packageBoxes) {
        this.batch = batch;
        J.emptyIfNull(packageBoxes).forEach(this::collect);
    }

    public Stream<StatisticsReport.Item> lineDiff() {
        final int lineCount = LINE_MAP.keySet().size();
        if (lineCount < 1) {
            log.error("全部为补充唛头，无法分配！");
            return Stream.empty();
        }
        if (lineCount == 1) {
            final Line line = IterableUtils.get(LINE_MAP.keySet(), 0);
            return GRADE_MAP.entrySet().parallelStream().map(entry -> {
                final Grade grade = entry.getKey();
                final Pair<Integer, BigDecimal> pair = entry.getValue();
                final StatisticsReport.Item item = new StatisticsReport.Item(line, batch, grade);
                item.setSilkCount(pair.getLeft());
                item.setSilkWeight(pair.getRight());
                return item;
            });
        }
        return GRADE_MAP.entrySet().parallelStream().flatMap(entry -> {
            final Grade grade = entry.getKey();
            final Pair<Integer, BigDecimal> pair = entry.getValue();
            final Integer silkCount = pair.getLeft();
            final BigDecimal silkWeight = pair.getRight();
            return DiffUtil.diff(batch, grade, silkCount, silkWeight, LINE_MAP);
        });
    }

    private void collect(PackageBox packageBox) {
        final Grade grade = packageBox.getGrade();
        final int silkCount = packageBox.getSilkCount();
        final BigDecimal netWeight = BigDecimal.valueOf(packageBox.getNetWeight());
        GRADE_MAP.compute(grade, (k, v) -> {
            if (v == null) {
                return Pair.of(silkCount, netWeight);
            } else {
                return Pair.of(silkCount + v.getLeft(), netWeight.add(v.getRight()));
            }
        });
        collectToLine(packageBox);
    }

    protected void collectToLine(PackageBox packageBox) {
        final Grade grade = packageBox.getGrade();
        final Collection<Silk> silks = packageBox.getSilks();
        if (J.nonEmpty(silks)) {
            silks.parallelStream().forEach(silk -> collectToLine(grade, silk));
            return;
        }
        J.emptyIfNull(packageBox.getSilkCarRecords()).parallelStream()
                .map(SilkCarRecord::initSilks)
                .filter(J::nonEmpty)
                .flatMap(Collection::parallelStream)
                .map(SilkRuntime::getSilk)
                .forEach(silk -> collectToLine(grade, silk));
    }

    protected void collectToLine(Grade grade, Silk silk) {
        final LineMachine lineMachine = silk.getLineMachine();
        final Line line = lineMachine.getLine();
        LINE_MAP.compute(line, (k1, v1) -> {
            final Map<Grade, Integer> gradeMap = v1 == null ? Maps.newConcurrentMap() : v1;
            gradeMap.compute(grade, (k2, v2) -> {
                final int count = v2 == null ? 0 : v2;
                return count + 1;
            });
            return gradeMap;
        });
    }
}
