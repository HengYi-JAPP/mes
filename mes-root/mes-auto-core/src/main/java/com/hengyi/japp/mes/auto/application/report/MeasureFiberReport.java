package com.hengyi.japp.mes.auto.application.report;

import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author jzb 2018-08-12
 */
@Data
public class MeasureFiberReport implements Serializable {
    private final Collection<Item> items;

    public MeasureFiberReport(Collection<Item> items) {
        items.parallelStream().map(item -> {
        })
        this.items = items;
    }

    @Data
    public static class Item implements Serializable {
        private final List<EventSource> eventSources;
        private final SilkCarRecord silkCarRecord;
        private final Operator operator;
    }
}
