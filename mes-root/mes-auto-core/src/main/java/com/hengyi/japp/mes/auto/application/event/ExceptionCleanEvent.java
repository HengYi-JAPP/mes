package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkException;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import com.hengyi.japp.mes.auto.domain.dto.SilkCarRecordDTO;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkExceptionRepository;
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
 * 清除异常
 *
 * @author jzb 2018-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ExceptionCleanEvent extends EventSource {
    @NotNull
    @Size(min = 1)
    private Collection<SilkRuntime> silkRuntimes;
    @NotNull
    @Size(min = 1)
    private Collection<SilkException> silkExceptions;

    @Override
    public Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        J.emptyIfNull(data).stream()
                .filter(J.emptyIfNull(silkRuntimes)::contains)
                .forEach(it -> it.removeException(silkExceptions));
        return data;
    }

    @Override
    protected Completable _undo(Operator operator) {
        return Completable.complete();
    }

    @Override
    public EventSourceType getType() {
        return EventSourceType.ExceptionCleanEvent;
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
        private Collection<EntityDTO> silkExceptions;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<ExceptionCleanEvent> toEvent() {
            final SilkExceptionRepository silkExceptionRepository = Jvertx.getProxy(SilkExceptionRepository.class);

            final ExceptionCleanEvent event = new ExceptionCleanEvent();
            return Flowable.fromIterable(silkRuntimes).flatMapSingle(SilkRuntime.DTO::rxToSilkRuntime).toList().flatMap(silkRuntimes -> {
                event.setSilkRuntimes(silkRuntimes);
                return Flowable.fromIterable(silkExceptions).map(EntityDTO::getId).flatMapSingle(silkExceptionRepository::find).toList();
            }).flatMap(silkExceptions -> {
                event.setSilkExceptions(silkExceptions);
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
        @NotNull
        @Size(min = 1)
        private Collection<EntityDTO> silkExceptions;

        public Single<ExceptionCleanEvent> toEvent(Principal principal) {
            final SilkExceptionRepository silkExceptionRepository = Jvertx.getProxy(SilkExceptionRepository.class);
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final ExceptionCleanEvent event = new ExceptionCleanEvent();
            return Flowable.fromIterable(silkRuntimes).flatMapSingle(SilkRuntime.DTO::rxToSilkRuntime).toList().flatMap(silkRuntimes -> {
                event.setSilkRuntimes(silkRuntimes);
                return Flowable.fromIterable(silkExceptions).map(EntityDTO::getId).flatMapSingle(silkExceptionRepository::find).toList();
            }).flatMap(silkExceptions -> {
                event.setSilkExceptions(silkExceptions);
                return operatorRepository.find(principal);
            }).map(it -> {
                event.fire(it);
                return event;
            });
        }
    }
}
