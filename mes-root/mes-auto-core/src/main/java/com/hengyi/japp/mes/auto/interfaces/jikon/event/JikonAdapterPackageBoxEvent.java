package com.hengyi.japp.mes.auto.interfaces.jikon.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.application.event.ExceptionCleanEvent;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class JikonAdapterPackageBoxEvent extends EventSource {
    @Override
    protected Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        return null;
    }

    @Override
    protected Completable _undo(Operator operator) {
        throw new IllegalAccessError();
    }

    @Override
    public EventSourceType getType() {
        return EventSourceType.JikonAdapterPackageBoxEvent;
    }

    @Override
    public JsonNode toJsonNode() {
        final ExceptionCleanEvent.DTO dto = MAPPER.convertValue(this, ExceptionCleanEvent.DTO.class);
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

        public Single<JikonAdapterPackageBoxEvent> toEvent() {
            final JikonAdapterPackageBoxEvent event = new JikonAdapterPackageBoxEvent();
            return toEvent(event);
        }
    }

    /**
     * @author jzb 2018-11-07
     */
    @Data
    public static class Command implements Serializable {
        @NotBlank
        private String boxCode;
        private String netWeight;
        private String grossWeight;
        private String grade;
        private String automaticPackeLine;
        private String classno;
        private String palletCode;
        private List<Item> spindle;

        @Data
        public static class Item {
            private String spindleCode;
            private String weight;
        }
    }
}
