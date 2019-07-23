package com.hengyi.japp.mes.auto.report.application.dto.silk_car_record;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableList;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.domain.data.SilkCarRecordAggregateType;
import lombok.SneakyThrows;
import org.bson.Document;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-07-11
 */
public class SilkCarRecordAggregate_History extends SilkCarRecordAggregate {

    protected SilkCarRecordAggregate_History(Document document) {
        super(document);
    }

    @Override
    protected Collection<EventSource.DTO> fetchEventSources() {
        return Optional.ofNullable(document.getString("events"))
                .filter(J::nonBlank)
                .map(this::toEventSources)
                .orElse(Collections.emptyList());
    }

    @SneakyThrows
    private Collection<EventSource.DTO> toEventSources(String s) {
        final ImmutableList.Builder<EventSource.DTO> builder = ImmutableList.builder();
        final JsonNode jsonNode = MAPPER.readTree(s);
        jsonNode.forEach(it -> builder.add(toEventSource(it)));
        return builder.build();
    }

    @Override
    public SilkCarRecordAggregateType getType() {
        return SilkCarRecordAggregateType.HISTORY;
    }

}
