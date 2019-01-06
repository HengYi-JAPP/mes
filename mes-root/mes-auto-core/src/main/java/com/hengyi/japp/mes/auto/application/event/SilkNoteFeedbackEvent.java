package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkNote;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import com.hengyi.japp.mes.auto.domain.dto.SilkCarRecordDTO;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkNoteRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
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
public class SilkNoteFeedbackEvent extends EventSource {
    private SilkNote silkNote;

    @Override
    public Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        return data;
    }

    @Override
    protected Completable _undo(Operator operator) {
        return Completable.complete();
    }

    @Override
    public EventSourceType getType() {
        return EventSourceType.SilkNoteFeedbackEvent;
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
        private EntityDTO silkNote;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<SilkNoteFeedbackEvent> toEvent() {
            final SilkNoteRepository silkNoteRepository = Jvertx.getProxy(SilkNoteRepository.class);

            final SilkNoteFeedbackEvent event = new SilkNoteFeedbackEvent();
            return silkNoteRepository.find(silkNote.getId()).flatMap(silkNote -> {
                event.setSilkNote(silkNote);
                return toEvent(event);
            });
        }
    }

    @Data
    public static class Command implements Serializable {
        @NotNull
        private SilkCarRecordDTO silkCarRecord;
        @NotNull
        private EntityDTO silkNote;

        public Single<SilkNoteFeedbackEvent> toEvent(Principal principal) {
            final SilkNoteRepository silkNoteRepository = Jvertx.getProxy(SilkNoteRepository.class);
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final SilkNoteFeedbackEvent event = new SilkNoteFeedbackEvent();
            return silkNoteRepository.find(silkNote.getId()).flatMap(silkNote -> {
                event.setSilkNote(silkNote);
                return operatorRepository.find(principal);
            }).map(it -> {
                event.fire(it);
                return event;
            });
        }
    }

}
