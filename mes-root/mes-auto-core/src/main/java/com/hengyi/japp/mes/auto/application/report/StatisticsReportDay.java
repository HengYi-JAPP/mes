package com.hengyi.japp.mes.auto.application.report;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.ComparisonChain;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.Workshop;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 日统计
 *
 * @author jzb 2018-08-12
 */
@Data
public class StatisticsReportDay implements Comparable<StatisticsReportDay>, Serializable {
    private final Workshop workshop;
    private final LocalDate ld;
    private final Collection<PackageBox> packageBoxes;
    private final int silkCount;
    private final BigDecimal silkWeight;
    private final Collection<StatisticsReport.Item> items;

    public StatisticsReportDay(Workshop workshop, LocalDate ld, Collection<PackageBox> packageBoxes) {
        this.workshop = workshop;
        this.ld = ld;
        this.packageBoxes = J.emptyIfNull(packageBoxes);
        silkCount = this.packageBoxes.parallelStream()
                .mapToInt(PackageBox::getSilkCount)
                .sum();
        silkWeight = this.packageBoxes.parallelStream()
                .map(PackageBox::getNetWeight)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        items = calcItems();
    }

    protected Collection<StatisticsReport.Item> calcItems() {
        return packageBoxes.parallelStream()
                .collect(Collectors.groupingBy(PackageBox::getBatch))
                .entrySet().parallelStream()
                .map(entry -> new StatisticsReportDay_Batch(entry.getKey(), entry.getValue()))
                .flatMap(StatisticsReportDay_Batch::lineDiff)
                .collect(Collectors.toList());
    }

    @Override
    public int compareTo(StatisticsReportDay o) {
        return ComparisonChain.start()
                .compare(ld, o.ld)
                .result();
    }

}
