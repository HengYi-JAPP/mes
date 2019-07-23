package com.hengyi.japp.mes.auto.interfaces.riamb.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class RiambSilkCarInfoFetchEvent extends EventSource {
    private String result;

    @Override
    public Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        return data;
    }

    @Override
    protected Completable _undo(Operator operator) {
        throw new IllegalAccessError();
    }

    @Override
    public EventSourceType getType() {
        return EventSourceType.RiambSilkCarInfoFetchEvent;
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
        private String result;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<RiambSilkCarInfoFetchEvent> toEvent() {
            final RiambSilkCarInfoFetchEvent event = new RiambSilkCarInfoFetchEvent();
            event.setResult(result);
            return toEvent(event);
        }
    }

}
