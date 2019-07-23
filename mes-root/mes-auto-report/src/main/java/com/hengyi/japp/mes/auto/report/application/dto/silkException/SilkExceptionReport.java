package com.hengyi.japp.mes.auto.report.application.dto.silkException;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.SilkException;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import lombok.Data;
import org.bson.Document;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jzb 2018-08-12
 */
@Data
public class SilkExceptionReport implements Serializable {

    private final List<Item> items;

    public SilkExceptionReport(List<Document> silks) {
        Map<String, List<Document>> groupMap = J.emptyIfNull(silks).parallelStream()
                .collect(Collectors.groupingBy(silk -> ((Document) silk.get("line")).getString("name") + "-" + ((Document) silk.get("batch")).getString("batchNo") + "-" + ((Document) silk.get("batch")).getString("spec"), Collectors.toList()));

        items = groupMap.keySet()
                .stream()
                .map(key -> {
                    String[] temp = key.split("-");
                    Map<Document, Long> silkExceptionMap = J.emptyIfNull(groupMap.get(key)).parallelStream()
                            .filter(silk -> silk.getString("exception") != null)
                            .filter(silk -> silk.getString("grade") != null)
                            .filter(silk -> {
                                Document grade = QueryService.find(Grade.class, silk.getString("grade")).block();
                                int sortBy = grade.getInteger("sortBy", 0);
                                if (sortBy < 100) {
                                    return true;
                                }
                                return false;
                            })
                            .collect(Collectors.groupingBy(silk -> {
                                Document exception = QueryService.find(SilkException.class, silk.getString("exception")).block();
                                return exception;
                            }, Collectors.counting()));
                    Map<Object, Long> gradeMap = J.emptyIfNull(groupMap.get(key)).parallelStream()
                            .filter(silk -> silk.getString("grade") != null)
                            .map(silk -> {
                                Document grade = QueryService.find(Grade.class, silk.getString("grade")).block();
                                return silk.append("grade", grade);
                            })
                            .filter(silk -> {
                                int sortBy = ((Document) silk.get("grade")).getInteger("sortBy", 0);
                                if (sortBy < 100) {
                                    return true;
                                }
                                return false;
                            })
                            .collect(Collectors.groupingBy(silk -> silk.get("grade"), Collectors.counting()));
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
        private final Document silkException;
        private final Integer exceptionCount;
    }

    @Data
    private final class GradeGroup {
        private final Object grade;
        private final Integer gradeCount;
    }
}
