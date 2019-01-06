package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.application.persistence.MongoEntity;
import com.hengyi.japp.mes.auto.application.persistence.annotations.JsonEntityProperty;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * 丝车，车次
 *
 * @author jzb 2018-06-20
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SilkCarRecord extends MongoEntity {
    @ToString.Include
    private SilkCar silkCar;
    private Batch batch;
    // 预设等级
    private Grade grade;

    private Operator doffingOperator;
    private DoffingType doffingType;
    private Date doffingDateTime;

    private Operator carpoolOperator;
    private Date carpoolDateTime;

    @JsonIgnore
    @JsonEntityProperty("initEvent")
    private String initEventJsonString;
    @JsonIgnore
    @JsonEntityProperty("events")
    private String eventsJsonString;

    private Date endDateTime;


//    @Transient
//    private SilkCarRuntimeInitEvent initEvent;

    public Date getStartDateTime() {
        return Optional.ofNullable(getDoffingDateTime()).orElse(getCarpoolDateTime());
    }

    @SneakyThrows
    @JsonGetter("initCommand")
    public JsonNode initCommand() {
        return Optional.ofNullable(getInitEventJsonString())
                .filter(J::nonBlank)
                .map(SilkCarRuntimeInitEvent.DTO::from)
                .map(SilkCarRuntimeInitEvent.DTO::getCommand)
                .orElse(null);
    }

    @SneakyThrows
    @JsonGetter("initSilks")
    public Collection<SilkRuntime> initSilks() {
        return Optional.ofNullable(getInitEventJsonString())
                .filter(J::nonBlank)
                .map(SilkCarRuntimeInitEvent.DTO::from)
                .map(SilkCarRuntimeInitEvent.DTO::getSilkRuntimes)
                .orElse(Collections.emptyList()).stream()
                .map(SilkRuntime.DTO::toSilkRuntime)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public void initEvent(EventSource initEvent) {
        final JsonNode jsonNode = Optional.ofNullable(initEvent)
                .map(EventSource::toJsonNode)
                .orElse(NullNode.getInstance());
        setInitEventJsonString(MAPPER.writeValueAsString(jsonNode));
    }

    @SneakyThrows
    public void events(Collection<EventSource> events) {
        final ArrayNode arrayNode = MAPPER.createArrayNode();
        J.emptyIfNull(events).stream().map(EventSource::toJsonNode).forEach(arrayNode::add);
        setEventsJsonString(MAPPER.writeValueAsString(arrayNode));
    }

}