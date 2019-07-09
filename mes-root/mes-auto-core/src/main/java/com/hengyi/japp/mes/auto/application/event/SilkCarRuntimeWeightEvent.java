package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.dto.SilkCarRecordDTO;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.github.ixtf.japp.core.Constant.MAPPER;


/**
 * 丝车称重
 *
 * @author jzb 2018-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SilkCarRuntimeWeightEvent extends EventSource {
    @NotNull
    @Size(min = 1)
    private List<Item> items;

    @Override
    public EventSourceType getType() {
        return EventSourceType.SilkCarRuntimeWeightEvent;
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
        private List<Item> items;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<SilkCarRuntimeWeightEvent> toEvent() {
            final SilkCarRuntimeWeightEvent event = new SilkCarRuntimeWeightEvent();
            event.setItems(items);
            return toEvent(event);
        }
    }

    @Data
    public static class Command implements Serializable {
        @NotNull
        private SilkCarRecordDTO silkCarRecord;
        @NotNull
        @Size(min = 1)
        private List<Item> items;
    }

    @Data
    public static class Item implements Serializable {
        @NotNull
        private EntityDTO lineMachine;
        @NotBlank
        private String doffingNum;
        @Min(1)
        private double weight;
    }

}
