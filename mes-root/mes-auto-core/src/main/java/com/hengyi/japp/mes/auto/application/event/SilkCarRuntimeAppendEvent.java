package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.dto.SilkCarRecordDTO;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;


/**
 * 两个半车
 *
 * @author jzb 2018-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SilkCarRuntimeAppendEvent extends EventSource {
    private Collection<SilkRuntime> silkRuntimes;
    private JsonNode command;

    @Override
    public EventSourceType getType() {
        return EventSourceType.SilkCarRuntimeAppendEvent;
    }

    @Override
    public JsonNode toJsonNode() {
        final DTO dto = MAPPER.convertValue(this, DTO.class);
        return MAPPER.convertValue(dto, JsonNode.class);
    }

    @Override
    public Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        final Stream<SilkRuntime> oldStream = J.emptyIfNull(data).stream();
        final Stream<SilkRuntime> appendStream = J.emptyIfNull(silkRuntimes).stream();
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
        private Collection<SilkRuntime.DTO> silkRuntimes;
        private JsonNode command;

        @SneakyThrows
        public static DTO from(String s) {
            return from(MAPPER.readTree(s));
        }

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<SilkCarRuntimeAppendEvent> toEvent() {
            final SilkCarRuntimeAppendEvent event = new SilkCarRuntimeAppendEvent();
            return Flowable.fromIterable(silkRuntimes)
                    .flatMapSingle(SilkRuntime.DTO::rxToSilkRuntime).toList()
                    .flatMap(silkRuntimes -> {
                        event.setSilkRuntimes(silkRuntimes);
                        return toEvent(event);
                    });
        }
    }

    @Data
    public static class CheckSilksCommand implements Serializable {
        @NotNull
        private SilkCarRecordDTO silkCarRecord;
        @Min(1)
        private float lineMachineCount;
    }

    @Data
    public static class Command implements Serializable {
        @NotNull
        private SilkCarRecordDTO silkCarRecord;
        @Min(1)
        private float lineMachineCount;
        @NotNull
        @Size(min = 1)
        private List<CheckSilkDTO> checkSilks;

        public Single<SilkCarRuntimeAppendEvent> toEvent(Principal principal) {
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final SilkCarRuntimeAppendEvent event = new SilkCarRuntimeAppendEvent();
            event.setCommand(MAPPER.convertValue(this, JsonNode.class));
            return operatorRepository.find(principal).map(it -> {
                event.fire(it);
                return event;
            });
        }
    }

}
