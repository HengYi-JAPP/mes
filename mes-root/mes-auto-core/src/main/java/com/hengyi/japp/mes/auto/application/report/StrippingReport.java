package com.hengyi.japp.mes.auto.application.report;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.domain.Operator;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuyuan
 * @create 2019-04-02 10:06
 * @description
 **/
@Data
public class StrippingReport implements Serializable {
    private final Collection<Item> items;

    public StrippingReport(List<EventSource> list) {
        items = J.emptyIfNull(list)
                .parallelStream()
                .collect(Collectors.groupingBy(EventSource::getOperator))
                .entrySet().parallelStream()
                .map(entry -> {
                    final Operator operator = entry.getKey();
                    final List<EventSource> eventSources = entry.getValue();
                    return new Item(operator, eventSources);
                })
                .collect(Collectors.toList());
    }

    @Data
    public static class Item implements Serializable {
        private final Operator operator;
        private final Collection<EventSource> eventSources;
    }
}
