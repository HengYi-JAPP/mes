package com.hengyi.japp.mes.auto.application.report;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.domain.*;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 日统计
 *
 * @author jzb 2018-08-12
 */
@Data
public class StatisticsReportDay implements Serializable {
    private final Workshop workshop;
    @JsonIgnore
    private final LocalDate ld;
    private final Collection<Item> items;

    public StatisticsReportDay(Workshop workshop, LocalDate ld, Collection<PackageBox> packageBoxes) {
        this.workshop = workshop;
        this.ld = ld;
        items = J.emptyIfNull(packageBoxes).stream()
                .collect(Collectors.groupingBy(PackageBox::getBatch))
                .entrySet().stream()
                .map(entry -> {
                    final Batch batch = entry.getKey();
                    final List<PackageBox> packageBoxList = entry.getValue();
                    return new StatisticsReportDayLineDiff(batch, packageBoxList);
                })
                .flatMap(StatisticsReportDayLineDiff::itemStream)
                .collect(Collectors.toList());
    }

    @JsonGetter("date")
    public Date ldJson() {
        return J.date(ld);
    }

    @Data
    public static class Item implements Serializable {
        private final Line line;
        private final Batch batch;
        private final Grade grade;
        private int silkCount;
        private BigDecimal silkWeight;
    }

    @Data
    public static class XlsxItem implements Serializable {
        private final Line line;
        private final Batch batch;
        private Map<Grade, Pair<Integer, BigDecimal>> gradePairMap = Maps.newConcurrentMap();
    }

}
