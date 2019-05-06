package com.hengyi.japp.mes.auto.application.report;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.domain.SilkException;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jzb 2018-11-26
 */
@Data
public class SilkExceptionReport implements Serializable {
    private final Collection<Item> items;

    public SilkExceptionReport(Collection<Silk> silks) {
        Map<String, List<Silk>> groupMap = J.emptyIfNull(silks).parallelStream()
                .collect(Collectors.groupingBy(silk -> silk.getLineMachine().getLine().getName() + "-" + silk.getBatch().getBatchNo() + "-" + silk.getBatch().getSpec(), Collectors.toList()));

        items = groupMap.keySet()
                .stream()
                .map(key -> {
                    String[] temp = key.split("-");
                    Map<SilkException, Long> silkExceptionMap = J.emptyIfNull(groupMap.get(key)).parallelStream()
                            .filter(silk -> silk.getException() != null)
                            .filter(silk -> silk.getGrade() != null || silk.getGrade().getSortBy() < 100)
                            .collect(Collectors.groupingBy(silk -> silk.getException(), Collectors.counting()));
                    Map<Grade, Long> gradeMap = J.emptyIfNull(groupMap.get(key)).parallelStream()
                            .filter(silk -> silk.getGrade() != null || silk.getGrade().getSortBy() < 100)
                            .collect(Collectors.groupingBy(silk -> silk.getGrade(), Collectors.counting()));
                    Collection<ExceptionGroup> exceptionGroups = silkExceptionMap.keySet()
                            .stream()
                            .map(key1 -> new ExceptionGroup(key1, silkExceptionMap.get(key1).intValue()))
                            .collect(Collectors.toList());
                    Collection<GradeGroup> gradeGroups = gradeMap.keySet()
                            .stream()
                            .map(key2 -> new GradeGroup(key2, gradeMap.get(key2).intValue()))
                            .collect(Collectors.toList());
                    Item item = new Item(temp[0], temp[1], temp[2], groupMap.get(key).size(), exceptionGroups, gradeGroups);
                    return item;
                }).collect(Collectors.toList());
    }

    @Data
    private final class Item {
        private final String lineName;
        private final String batchNo;
        private final String spec;
        private final Integer totalCount;
        private final Collection<ExceptionGroup> exceptionGroups;
        private final Collection<GradeGroup> gradeGroups;
    }

    @Data
    private final class ExceptionGroup {
        private final SilkException silkException;
        private final Integer exceptionCount;
    }

    @Data
    private final class GradeGroup {
        private final Grade grade;
        private final Integer gradeCount;
    }
}
