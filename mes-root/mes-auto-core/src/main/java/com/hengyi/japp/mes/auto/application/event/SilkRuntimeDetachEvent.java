package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.dto.SilkCarRecordDTO;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * 丝锭解绑
 *
 * @author jzb 2018-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SilkRuntimeDetachEvent extends EventSource {
    @NotNull
    @Size(min = 1)
    private Collection<SilkRuntime> silkRuntimes;

    @Override
    protected Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        data = Sets.newHashSet(J.emptyIfNull(data));
        data.removeAll(J.emptyIfNull(silkRuntimes));
        return data;
    }

    @Override
    protected Completable _undo(Operator operator) {
        final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);

        return Flowable.fromIterable(J.emptyIfNull(silkRuntimes))
                .map(SilkRuntime::getSilk).toList()
                .flatMapCompletable(silks -> {
                    silks.forEach(silk -> {
                        if (!silk.isDetached()) {
                            throw new RuntimeException("丝锭silk[" + silk.getCode() + "]处于非解绑状态");
                        }
                        silk.setDetached(false);
                    });
                    return Flowable.fromIterable(silks).flatMapSingle(silkRepository::save).toList().ignoreElement();
                });
    }

    @Override
    public EventSourceType getType() {
        return EventSourceType.SilkRuntimeDetachEvent;
    }

    @Override
    public JsonNode toJsonNode() {
        final DTO dto = MAPPER.convertValue(this, DTO.class);
        return MAPPER.convertValue(dto, JsonNode.class);
    }

    @Data
    @ToString(onlyExplicitlyIncluded = true)
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class DTO extends EventSource.DTO {
        private Collection<SilkRuntime.DTO> silkRuntimes;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<SilkRuntimeDetachEvent> toEvent() {
            final SilkRuntimeDetachEvent event = new SilkRuntimeDetachEvent();
            return Flowable.fromIterable(J.emptyIfNull(silkRuntimes))
                    .flatMapSingle(SilkRuntime.DTO::rxToSilkRuntime).toList()
                    .flatMap(silkRuntimes -> {
                        event.setSilkRuntimes(silkRuntimes);
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
        private Collection<SilkRuntime.DTO> silkRuntimes;

        public Single<SilkRuntimeDetachEvent> toEvent(Principal principal) {
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final SilkRuntimeDetachEvent event = new SilkRuntimeDetachEvent();
            return Flowable.fromIterable(J.emptyIfNull(silkRuntimes))
                    .flatMapSingle(SilkRuntime.DTO::rxToSilkRuntime).toList()
                    .flatMap(silkRuntimes -> {
                        event.setSilkRuntimes(silkRuntimes);
                        return operatorRepository.find(principal);
                    }).map(it -> {
                        event.fire(it);
                        return event;
                    });
        }
    }

}
