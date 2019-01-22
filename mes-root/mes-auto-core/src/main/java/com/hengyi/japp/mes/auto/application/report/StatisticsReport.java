package com.hengyi.japp.mes.auto.application.report;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Multimap;
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
    private final Collection<Item> items;

    public StatisticsReport(Workshop workshop, LocalDate startLd, LocalDate endLd, Collection<StatisticsReportDay> days) {
        this.workshop = workshop;
        this.startLd = startLd;
        this.endLd = endLd;
        items = J.emptyIfNull(days).parallelStream()
                .map(StatisticsReportDay::getItems)
                .map(J::emptyIfNull)
                .flatMap(Collection::parallelStream)
                .collect(Collectors.groupingBy(it -> {
                    final Line line = it.getLine();
                    final Batch batch = it.getBatch();
                    final Grade grade = it.getGrade();
                    return Triple.of(line, batch, grade);
                }))
                .entrySet().parallelStream()
                .map(entry -> {
                    final Triple<Line, Batch, Grade> triple = entry.getKey();
                    final Line line = triple.getLeft();
                    final Batch batch = triple.getMiddle();
                    final Grade grade = triple.getRight();
                    final Item result = new Item(line, batch, grade);
                    final BigDecimal silkWeight = J.emptyIfNull(entry.getValue()).stream()
                            .map(Item::getSilkWeight)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    result.setSilkWeight(silkWeight);
                    final int silkCount = J.emptyIfNull(entry.getValue()).stream()
                            .mapToInt(Item::getSilkCount)
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

    @Data
    public static class Item implements Comparable<Item>, Serializable {
        private final Line line;
        private final Batch batch;
        private final Grade grade;
        private int silkCount;
        private BigDecimal silkWeight;

        @Override
        public int compareTo(Item o) {
            return ComparisonChain.start()
                    .compare(line.getName(), o.line.getName())
                    .compare(batch.getBatchNo(), o.batch.getBatchNo())
                    .result();
        }
    }

    @Data
    public static class XlsxItem implements Comparable<XlsxItem>, Serializable {
        private final Line line;
        private final Multimap<Batch, Triple<Grade, Integer, BigDecimal>> batchMultimap;

        @Override
        public int compareTo(XlsxItem o) {
            return ComparisonChain.start()
                    .compare(line.getName(), o.line.getName())
                    .result();
        }
    }
}
