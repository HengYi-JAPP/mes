package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import com.hengyi.japp.mes.auto.domain.dto.SilkCarRecordDTO;
import com.hengyi.japp.mes.auto.repository.GradeRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Objects;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-07-30
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SilkCarRuntimeGradeSubmitEvent extends EventSource {
    @NotNull
    private Collection<Item> silkRuntimes;

    @Override
    protected Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        J.emptyIfNull(silkRuntimes).forEach(item -> {
            final Grade thisGrade = item.getGrade();
            J.emptyIfNull(data).stream()
                    .filter(it -> Objects.equals(it.getSilk(), item.getSilk()))
                    .findFirst()
                    .ifPresent(silkRuntime -> {
                        final Grade grade = silkRuntime.getGrade();
                        if (grade == null) {
                            silkRuntime.setGrade(thisGrade);
                        } else if (thisGrade.getSortBy() < grade.getSortBy()) {
                            silkRuntime.setGrade(thisGrade);
                        }
                    });
        });
        return data;
    }

    @Override
    protected Completable _undo(Operator operator) {
        return Completable.complete();
    }

    @Override
    public EventSourceType getType() {
        return EventSourceType.SilkCarRuntimeGradeSubmitEvent;
    }

    @Override
    public JsonNode toJsonNode() {
        final DTO dto = MAPPER.convertValue(this, DTO.class);
        return MAPPER.convertValue(dto, JsonNode.class);
    }

    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class Item extends SilkRuntime {
        private Grade grade;
    }

    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class ItemDTO extends SilkRuntime.DTO {
        @NotNull
        private EntityDTO grade;
    }

    @Data
    @ToString(onlyExplicitlyIncluded = true)
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class DTO extends EventSource.DTO {
        private Collection<ItemDTO> silkRuntimes;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<SilkCarRuntimeGradeSubmitEvent> toEvent() {
            final GradeRepository gradeRepository = Jvertx.getProxy(GradeRepository.class);

            final SilkCarRuntimeGradeSubmitEvent event = new SilkCarRuntimeGradeSubmitEvent();
            return Flowable.fromIterable(J.emptyIfNull(silkRuntimes)).flatMapSingle(dto -> dto.rxToSilkRuntime().flatMap(silkRuntime -> {
                final Item item = MAPPER.convertValue(silkRuntime, Item.class);
                return gradeRepository.find(dto.getGrade().getId()).map(grade -> {
                    item.setGrade(grade);
                    return item;
                });
            })).toList().flatMap(items -> {
                event.setSilkRuntimes(items);
                return toEvent(event);
            });
        }
    }

    @Data
    public static class Command implements Serializable {
        @NotNull
        private SilkCarRecordDTO silkCarRecord;
        @NotNull
        private Collection<ItemDTO> silkRuntimes;

        public Single<SilkCarRuntimeGradeSubmitEvent> toEvent(Principal principal) {
            final GradeRepository gradeRepository = Jvertx.getProxy(GradeRepository.class);
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final SilkCarRuntimeGradeSubmitEvent event = new SilkCarRuntimeGradeSubmitEvent();
            return Flowable.fromIterable(J.emptyIfNull(silkRuntimes)).flatMapSingle(dto -> dto.rxToSilkRuntime().flatMap(silkRuntime -> {
                final Item item = MAPPER.convertValue(silkRuntime, Item.class);
                return gradeRepository.find(dto.getGrade().getId()).map(grade -> {
                    item.setGrade(grade);
                    return item;
                });
            })).toList().flatMap(silkRuntimes -> {
                event.setSilkRuntimes(silkRuntimes);
                return operatorRepository.find(principal);
            }).map(it -> {
                event.fire(it);
                return event;
            });
        }
    }

}
