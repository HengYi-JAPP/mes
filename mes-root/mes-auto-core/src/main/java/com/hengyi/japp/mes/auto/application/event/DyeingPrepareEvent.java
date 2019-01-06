package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.DyeingType;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import com.hengyi.japp.mes.auto.domain.dto.SilkCarRecordDTO;
import com.hengyi.japp.mes.auto.repository.DyeingPrepareRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.ixtf.japp.core.Constant.MAPPER;


/**
 * @author jzb 2018-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DyeingPrepareEvent extends EventSource {
    private DyeingPrepare dyeingPrepare;
    private JsonNode command;

    @Override
    public EventSourceType getType() {
        return EventSourceType.DyeingPrepareEvent;
    }

    @Override
    public JsonNode toJsonNode() {
        final DTO dto = MAPPER.convertValue(this, DTO.class);
        return MAPPER.convertValue(dto, JsonNode.class);
    }

    @Override
    public Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        if (dyeingPrepare.isMulti()) {
            final Map<Silk, DyeingResult> map = dyeingPrepare.getDyeingResults().stream()
                    .collect(Collectors.toMap(DyeingResult::getSilk, Function.identity()));
            J.emptyIfNull(data).stream().forEach(silkRuntime -> {
                final Silk silk = silkRuntime.getSilk();
                final SilkRuntime.DyeingResultInfo multiDyeingResultInfo = new SilkRuntime.DyeingResultInfo();
                multiDyeingResultInfo.setDyeingResult(map.get(silk));
                silkRuntime.setMultiDyeingResultInfo(multiDyeingResultInfo);
            });
        }
        return data;
    }

    @Override
    protected Completable _undo(Operator operator) {
        // todo 织袜暂时不支持撤销
        throw new IllegalAccessError();
    }

    @Data
    @ToString(onlyExplicitlyIncluded = true)
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class DTO extends EventSource.DTO {
        private EntityDTO dyeingPrepare;
        private JsonNode command;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<DyeingPrepareEvent> toEvent() {
            final DyeingPrepareRepository dyeingPrepareRepository = Jvertx.getProxy(DyeingPrepareRepository.class);

            final DyeingPrepareEvent event = new DyeingPrepareEvent();
            return dyeingPrepareRepository.find(dyeingPrepare.getId()).flatMap(dyeingPrepare -> {
                event.setDyeingPrepare(dyeingPrepare);
                return toEvent(event);
            });
        }
    }

    @Data
    public static class FirstSubmitCommand implements Serializable {
        @NotNull
        protected SilkCarRecordDTO silkCarRecord;
        @NotNull
        @Size(min = 1)
        protected Collection<SilkRuntime.DTO> silkRuntimes;

        public Single<DyeingPrepareEvent> toEvent(Principal principal) {
            final DyeingPrepareRepository dyeingPrepareRepository = Jvertx.getProxy(DyeingPrepareRepository.class);
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final DyeingPrepareEvent event = new DyeingPrepareEvent();
            event.setCommand(MAPPER.convertValue(this, JsonNode.class));
            return operatorRepository.find(principal).flatMap(operator -> {
                event.fire(operator);
                return dyeingPrepareRepository.create();
            }).map(dyeingPrepare -> {
                dyeingPrepare.setType(DyeingType.FIRST);
                dyeingPrepare.setCreator(event.getOperator());
                dyeingPrepare.setCreateDateTime(event.getFireDateTime());
                event.setDyeingPrepare(dyeingPrepare);
                return event;
            });
        }

        @Data
        public static class Batch implements Serializable {
            @NotNull
            @Size(min = 1)
            private Collection<FirstSubmitCommand> commands;
        }
    }

    @Data
    public static class CrossSpindleSubmitCommand implements Serializable {
        @NotNull
        protected SilkCarRecordDTO silkCarRecord;
        @NotNull
        @Size(min = 1)
        protected Collection<SilkRuntime.DTO> silkRuntimes;

        public Single<DyeingPrepareEvent> toEvent(Principal principal) {
            final DyeingPrepareRepository dyeingPrepareRepository = Jvertx.getProxy(DyeingPrepareRepository.class);
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final DyeingPrepareEvent event = new DyeingPrepareEvent();
            event.setCommand(MAPPER.convertValue(this, JsonNode.class));
            return operatorRepository.find(principal).flatMap(operator -> {
                event.fire(operator);
                return dyeingPrepareRepository.create();
            }).map(dyeingPrepare -> {
                dyeingPrepare.setType(DyeingType.CROSS_LINEMACHINE_SPINDLE);
                dyeingPrepare.setCreator(event.getOperator());
                dyeingPrepare.setCreateDateTime(event.getFireDateTime());
                event.setDyeingPrepare(dyeingPrepare);
                return event;
            });
        }

        @Data
        public static class Batch implements Serializable {
            @NotNull
            @Size(min = 1)
            private Collection<CrossSpindleSubmitCommand> commands;
        }
    }

    @Data
    public static class CrossLineMachineSubmitCommand implements Serializable {
        @NotNull
        private SilkCarRecordDTO silkCarRecord1;
        @NotNull
        @Size(min = 1)
        private Collection<SilkRuntime.DTO> silkRuntimes1;
        @NotNull
        private SilkCarRecordDTO silkCarRecord2;
        @NotNull
        @Size(min = 1)
        private Collection<SilkRuntime.DTO> silkRuntimes2;

        public Single<DyeingPrepareEvent> toEvent(Principal principal) {
            if (getSilkRuntimes1().size() != getSilkRuntimes2().size()) {
                throw new RuntimeException("交织颗数不一致");
            }

            final DyeingPrepareRepository dyeingPrepareRepository = Jvertx.getProxy(DyeingPrepareRepository.class);
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final DyeingPrepareEvent event = new DyeingPrepareEvent();
            event.setCommand(MAPPER.convertValue(this, JsonNode.class));
            return operatorRepository.find(principal).flatMap(operator -> {
                event.fire(operator);
                return dyeingPrepareRepository.create();
            }).map(dyeingPrepare -> {
                dyeingPrepare.setType(DyeingType.CROSS_LINEMACHINE_LINEMACHINE);
                dyeingPrepare.setCreator(event.getOperator());
                dyeingPrepare.setCreateDateTime(event.getFireDateTime());
                event.setDyeingPrepare(dyeingPrepare);
                return event;
            });
        }
    }

    @Data
    public static class MultiSubmitCommand implements Serializable {
        @NotNull
        private SilkCarRecordDTO silkCarRecord;
        @NotNull
        @Size(min = 1)
        private Collection<SilkRuntime.DTO> silkRuntimes;
        @NotNull
        private DyeingType dyeingType;

        public Single<DyeingPrepareEvent> toEvent(Principal principal) {
            final DyeingPrepareRepository dyeingPrepareRepository = Jvertx.getProxy(DyeingPrepareRepository.class);
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final DyeingPrepareEvent event = new DyeingPrepareEvent();
            event.setCommand(MAPPER.convertValue(this, JsonNode.class));
            return operatorRepository.find(principal).flatMap(operator -> {
                event.fire(operator);
                return dyeingPrepareRepository.create();
            }).map(dyeingPrepare -> {
                dyeingPrepare.setType(dyeingType);
                dyeingPrepare.setCreator(event.getOperator());
                dyeingPrepare.setCreateDateTime(event.getFireDateTime());
                event.setDyeingPrepare(dyeingPrepare);
                return event;
            });
        }
    }

}
