package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.dto.SilkCarRecordDTO;
import com.hengyi.japp.mes.auto.repository.GradeRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
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
 * 丝车预设等级
 *
 * @author jzb 2018-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SilkCarRuntimeGradeEvent extends EventSource {
    @NotNull
    private Grade grade;

    @Override
    public EventSourceType getType() {
        return EventSourceType.SilkCarRuntimeGradeEvent;
    }

    @Override
    public JsonNode toJsonNode() {
        final DTO dto = MAPPER.convertValue(this, DTO.class);
        return MAPPER.convertValue(dto, JsonNode.class);
    }

    @Override
    public Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        return data;
    }

    @Override
    protected Completable _undo(Operator operator) {
        throw new IllegalAccessError();
    }

    @Data
    @ToString(onlyExplicitlyIncluded = true)
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class DTO extends EventSource.DTO {
        private EntityDTO grade;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<SilkCarRuntimeGradeEvent> toEvent() {
            final GradeRepository gradeRepository = Jvertx.getProxy(GradeRepository.class);

            final SilkCarRuntimeGradeEvent event = new SilkCarRuntimeGradeEvent();
            return gradeRepository.find(grade.getId()).flatMap(grade -> {
                event.setGrade(grade);
                return toEvent(event);
            });
        }
    }

    @Data
    public static class Command implements Serializable {
        @NotNull
        private SilkCarRecordDTO silkCarRecord;
        @NotNull
        private EntityDTO grade;

        public Single<SilkCarRuntimeGradeEvent> toEvent(Principal principal) {
            final GradeRepository gradeRepository = Jvertx.getProxy(GradeRepository.class);
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final SilkCarRuntimeGradeEvent event = new SilkCarRuntimeGradeEvent();
            return gradeRepository.find(grade.getId()).flatMap(grade -> {
                event.setGrade(grade);
                return operatorRepository.find(principal);
            }).map(it -> {
                event.fire(it);
                return event;
            });
        }
    }

}
