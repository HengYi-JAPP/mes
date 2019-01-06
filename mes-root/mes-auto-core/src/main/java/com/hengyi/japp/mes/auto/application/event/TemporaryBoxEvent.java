package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.TemporaryBoxRecord;
import com.hengyi.japp.mes.auto.domain.data.TemporaryBoxRecordType;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import com.hengyi.japp.mes.auto.domain.dto.SilkCarRecordDTO;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.TemporaryBoxRecordRepository;
import com.hengyi.japp.mes.auto.repository.TemporaryBoxRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;

import static com.github.ixtf.japp.core.Constant.MAPPER;


/**
 * @author jzb 2018-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class TemporaryBoxEvent extends EventSource {
    private TemporaryBoxRecord temporaryBoxRecord;
    private JsonNode command;

    @Override
    public Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        return Collections.emptyList();
    }

    @Override
    protected Completable _undo(Operator operator) {
        throw new IllegalAccessError();
    }

    @Override
    public EventSourceType getType() {
        return EventSourceType.TemporaryBoxEvent;
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
        private EntityDTO temporaryBoxRecord;
        private JsonNode command;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<TemporaryBoxEvent> toEvent() {
            final TemporaryBoxRecordRepository temporaryBoxRecordRepository = Jvertx.getProxy(TemporaryBoxRecordRepository.class);

            final TemporaryBoxEvent event = new TemporaryBoxEvent();
            event.setCommand(command);
            return temporaryBoxRecordRepository.find(temporaryBoxRecord.getId()).flatMap(temporaryBoxRecord -> {
                event.setTemporaryBoxRecord(temporaryBoxRecord);
                return toEvent(event);
            });
        }
    }

    @Data
    public static class FromSilkCarRuntimeCommand implements Serializable {
        @NotNull
        private EntityDTO temporaryBox;
        @NotNull
        private SilkCarRecordDTO silkCarRecord;

        public Single<TemporaryBoxEvent> toEvent(Principal principal) {
            final TemporaryBoxRecordRepository temporaryBoxRecordRepository = Jvertx.getProxy(TemporaryBoxRecordRepository.class);
            final TemporaryBoxRepository temporaryBoxRepository = Jvertx.getProxy(TemporaryBoxRepository.class);
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final TemporaryBoxEvent event = new TemporaryBoxEvent();
            event.setCommand(MAPPER.convertValue(this, JsonNode.class));
            return temporaryBoxRecordRepository.create().flatMap(temporaryBoxRecord -> temporaryBoxRepository.find(temporaryBox.getId()).flatMap(temporaryBox -> {
                temporaryBoxRecord.setTemporaryBox(temporaryBox);
                temporaryBoxRecord.setType(TemporaryBoxRecordType.INCREMENT_BY_SILKCARRUNTIME);
                event.setTemporaryBoxRecord(temporaryBoxRecord);
                return operatorRepository.find(principal);
            })).map(it -> {
                event.fire(it);
                return event;
            });
        }
    }

}
