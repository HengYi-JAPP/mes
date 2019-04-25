package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.dto.EntityByCodeDTO;
import com.hengyi.japp.mes.auto.dto.SilkCarRecordDTO;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.toSet;


/**
 * 两个半车
 *
 * @author jzb 2018-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class BigSilkCarSilkChangeEvent extends EventSource {
    private Collection<SilkRuntime> inSilkRuntimes;
    private Collection<SilkRuntime> outSilkRuntimes;
    private JsonNode command;

    @Override
    public EventSourceType getType() {
        return EventSourceType.BigSilkCarSilkChangeEvent;
    }

    @Override
    public JsonNode toJsonNode() {
        final DTO dto = MAPPER.convertValue(this, DTO.class);
        return MAPPER.convertValue(dto, JsonNode.class);
    }

    @Override
    public Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        final var outSilkIds = outSilkRuntimes.parallelStream().map(SilkRuntime::getSilk).map(Silk::getId).collect(toSet());
        final Stream<SilkRuntime> oldStream = J.emptyIfNull(data).stream().filter(silkRuntime -> {
            final Silk silk = silkRuntime.getSilk();
            @NotBlank final String id = silk.getId();
            return !outSilkIds.contains(id);
        });
        final Stream<SilkRuntime> appendStream = J.emptyIfNull(inSilkRuntimes).stream();
        return Stream.concat(oldStream, appendStream).collect(Collectors.toList());
    }

    @Override
    protected Completable _undo(Operator operator) {
        throw new IllegalAccessError();
    }

    @Data
    @ToString(onlyExplicitlyIncluded = true)
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class DTO extends EventSource.DTO {
        private Collection<SilkRuntime.DTO> inSilkRuntimes;
        private Collection<SilkRuntime.DTO> outSilkRuntimes;
        private JsonNode command;

        @SneakyThrows
        public static DTO from(String s) {
            return from(MAPPER.readTree(s));
        }

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<BigSilkCarSilkChangeEvent> toEvent() {
            final BigSilkCarSilkChangeEvent event = new BigSilkCarSilkChangeEvent();
            return Flowable.fromIterable(inSilkRuntimes)
                    .flatMapSingle(SilkRuntime.DTO::rxToSilkRuntime).toList()
                    .flatMap(it -> {
                        event.setInSilkRuntimes(it);
                        return Flowable.fromIterable(outSilkRuntimes)
                                .flatMapSingle(SilkRuntime.DTO::rxToSilkRuntime).toList();
                    }).flatMap(it -> {
                        event.setOutSilkRuntimes(it);
                        return toEvent(event);
                    });
        }
    }

    @Data
    public static class Command implements Serializable {
        @NotNull
        private SilkCarRecordDTO silkCarRecord;
        @NotNull
        @Size(min = 1)
        private List<EntityByCodeDTO> inSilks;
        @NotNull
        @Size(min = 1)
        private List<EntityByCodeDTO> outSilks;
    }
}
