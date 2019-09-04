package com.hengyi.japp.mes.auto.report.application.dto.silk_car_record;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableList;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeAppendEvent;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.SilkCarRecordAggregateType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-07-11
 */
@Slf4j
@Data
public abstract class SilkCarRecordAggregate implements Serializable {
    protected final Document document;
    protected final String id;
    protected final String doffingOperatorId;
    protected final Date doffingDateTime;
    protected final String carpoolOperatorId;
    protected final Date carpoolDateTime;
    protected final Document creator;
    protected final Date startDateTime;
    protected final Date endDateTime;
    protected final Document silkCar;
    protected final Document batch;
    protected final Document grade;
    protected final Collection<EventSource.DTO> eventSourceDtos;
    protected final Collection<SilkRuntime.DTO> initSilkRuntimeDtos;

    protected SilkCarRecordAggregate(Document document) {
        this.document = document;
        id = document.getString(ID_COL);
        doffingOperatorId = document.getString("doffingOperator");
        doffingDateTime = document.getDate("doffingDateTime");
        carpoolOperatorId = document.getString("carpoolOperator");
        carpoolDateTime = document.getDate("carpoolDateTime");
        creator = QueryService.findFromCache(Operator.class, Optional.ofNullable(doffingOperatorId).orElse(carpoolOperatorId)).get();
        startDateTime = Optional.ofNullable(doffingDateTime).orElse(carpoolDateTime);
        endDateTime = document.getDate("endDateTime");
        silkCar = QueryService.findFromCache(SilkCar.class, document.getString("silkCar")).get();
        batch = QueryService.findFromCache(Batch.class, document.getString("batch")).get();
        grade = QueryService.findFromCache(Grade.class, document.getString("grade")).get();
        eventSourceDtos = J.emptyIfNull(fetchEventSources());
        initSilkRuntimeDtos = J.emptyIfNull(fetchInitSilkRuntimes());
    }

    public static Mono<SilkCarRecordAggregate> from(String id) {
        return QueryService.find(SilkCarRecord.class, id).map(document ->
                document.getDate("endDateTime") != null
                        ? new SilkCarRecordAggregate_History(document)
                        : new SilkCarRecordAggregate_Runtime(document)
        );
    }

    private Collection<SilkRuntime.DTO> fetchInitSilkRuntimes() {
        final ImmutableList.Builder<SilkRuntime.DTO> builder = ImmutableList.builder();
        final SilkCarRuntimeInitEvent.DTO initEventDto = SilkCarRuntimeInitEvent.DTO.from(document.getString("initEvent"));
        builder.addAll(initEventDto.getSilkRuntimes());
        final Collection<SilkRuntime.DTO> appends = eventSourceDtos.parallelStream()
                .filter(it -> !it.isDeleted() && EventSourceType.SilkCarRuntimeAppendEvent == it.getType())
                .flatMap(it -> {
                    final SilkCarRuntimeAppendEvent.DTO event = (SilkCarRuntimeAppendEvent.DTO) it;
                    return J.emptyIfNull(event.getSilkRuntimes()).parallelStream();
                })
                .collect(toList());
        builder.addAll(appends);
        return builder.build();
    }

    protected abstract Collection<EventSource.DTO> fetchEventSources();

    @SneakyThrows
    public static ObjectNode toJsonNode(SilkRuntime.DTO dto) {
        final ObjectNode objectNode = MAPPER.createObjectNode()
                .put("sideType", Optional.ofNullable(dto.getSideType()).map(SilkCarSideType::name).orElse(null))
                .put("row", dto.getRow())
                .put("col", dto.getCol());
        final Document silk = QueryService.find(Silk.class, dto.getSilk()).block();
        if (silk == null) {
            log.error("");
            return objectNode;
        }
        objectNode.set("silk", MAPPER.readTree(silk.toJson()));
        return objectNode;
    }

    @SneakyThrows
    protected EventSource.DTO toEventSource(String s) {
        return toEventSource(MAPPER.readTree(s));
    }

    protected EventSource.DTO toEventSource(JsonNode jsonNode) {
        final EventSourceType type = EventSourceType.valueOf(jsonNode.get("type").asText());
        return type.toDto(jsonNode);
    }

    public boolean hasEventSource(EventSourceType type) {
        return eventSourceDtos.parallelStream()
                .filter(it -> !it.isDeleted() && it.getType() == type)
                .findFirst().isPresent();
    }

    @SneakyThrows
    public static ObjectNode toJsonNode(EventSource.DTO dto) {
        final ObjectNode objectNode = MAPPER.createObjectNode()
                .put("eventId", dto.getEventId())
                .put("type", dto.getType().name())
                .put("fireDateTime", dto.getFireDateTime().getTime())
                .put("deleted", dto.isDeleted())
                .put("deleteDateTime", Optional.ofNullable(dto.getDeleteDateTime()).map(Date::getTime).orElse(null));
        final Document operator = QueryService.findFromCache(Operator.class, dto.getOperator()).get();
        objectNode.set("operator", MAPPER.readTree(operator.toJson()));
        if (dto.isDeleted()) {
            final Document deleteOperator = QueryService.findFromCache(Operator.class, dto.getDeleteOperator()).get();
            objectNode.set("deleteOperator", MAPPER.readTree(deleteOperator.toJson()));
        }
        return objectNode;
    }

    public abstract SilkCarRecordAggregateType getType();

    @SneakyThrows
    public ObjectNode toJsonNode() {
        final ObjectNode objectNode = MAPPER.createObjectNode()
                .put("type", getType().name())
                .put("id", document.getString(ID_COL))
                .put("startDateTime", startDateTime.getTime())
                .put("endDateTime", Optional.ofNullable(endDateTime).map(Date::getTime).orElse(null));
        objectNode.set("silkCar", MAPPER.readTree(silkCar.toJson()));
        objectNode.set("batch", MAPPER.readTree(batch.toJson()));
        objectNode.set("grade", MAPPER.readTree(grade.toJson()));
        objectNode.set("creator", MAPPER.readTree(creator.toJson()));
        final ArrayNode initSilkRuntimesArrayNode = MAPPER.createArrayNode();
        objectNode.set("initSilkRuntimes", initSilkRuntimesArrayNode);
        J.emptyIfNull(initSilkRuntimeDtos).stream()
                .map(SilkCarRecordAggregate::toJsonNode)
                .forEach(initSilkRuntimesArrayNode::add);
        final ArrayNode eventSourcesArrayNode = MAPPER.createArrayNode();
        objectNode.set("eventSources", eventSourcesArrayNode);
        J.emptyIfNull(eventSourceDtos).stream()
                .map(SilkCarRecordAggregate::toJsonNode)
                .forEach(eventSourcesArrayNode::add);
        return objectNode;
    }

}
