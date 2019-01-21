package com.hengyi.japp.mes.auto.application.report;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.Workshop;
import lombok.Data;
import org.apache.commons.lang3.tuple.Triple;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * @author jzb 2018-08-12
 */
@Data
public class StatisticsReport implements Serializable {
    private final Workshop workshop;
    @JsonIgnore
    private final LocalDate startLd;
    @JsonIgnore
    private final LocalDate endLd;
    private final Collection<StatisticsReportDay.Item> items;

    public StatisticsReport(Workshop workshop, LocalDate startLd, LocalDate endLd, Collection<StatisticsReportDay> days) {
        this.workshop = workshop;
        this.startLd = startLd;
        this.endLd = endLd;
        items = J.emptyIfNull(days).stream()
                .map(StatisticsReportDay::getItems)
                .map(J::emptyIfNull)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(it -> {
                    final Line line = it.getLine();
                    final Batch batch = it.getBatch();
                    final Grade grade = it.getGrade();
                    return Triple.of(line, batch, grade);
                }))
                .entrySet().stream()
                .map(entry -> {
                    final Triple<Line, Batch, Grade> triple = entry.getKey();
                    final Line line = triple.getLeft();
                    final Batch batch = triple.getMiddle();
                    final Grade grade = triple.getRight();
                    final StatisticsReportDay.Item result = new StatisticsReportDay.Item(line, batch, grade);
                    final BigDecimal silkWeight = J.emptyIfNull(entry.getValue()).stream()
                            .map(StatisticsReportDay.Item::getSilkWeight)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    result.setSilkWeight(silkWeight);
                    final int silkCount = J.emptyIfNull(entry.getValue()).stream()
                            .mapToInt(StatisticsReportDay.Item::getSilkCount)
                            .sum();
                    result.setSilkCount(silkCount);
                    return result;
                })
                .collect(Collectors.toList());
    }

    @JsonGetter("startDate")
    public Date startLdJson() {
        return J.date(startLd);
    }

    @JsonGetter("endDate")
    public Date endLdJson() {
        return J.date(endLd);
    }

}
