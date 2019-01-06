package com.hengyi.japp.mes.auto.interfaces.jikon.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class JikonAdapterSilkCarInfoFetchEvent extends EventSource {
    private JsonNode command;
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
        return EventSourceType.JikonAdapterSilkCarInfoFetchEvent;
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
        private JsonNode command;
        private String result;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<JikonAdapterSilkCarInfoFetchEvent> toEvent() {
            final JikonAdapterSilkCarInfoFetchEvent event = new JikonAdapterSilkCarInfoFetchEvent();
            event.setCommand(command);
            event.setResult(result);
            return toEvent(event);
        }
    }

    /**
     * @author jzb 2018-11-07
     */
    @Data
    public static class Command implements Serializable {
        @NotBlank
        private String silkcarCode;

        public Single<JikonAdapterSilkCarInfoFetchEvent> toEvent(Principal principal) {
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final JikonAdapterSilkCarInfoFetchEvent event = new JikonAdapterSilkCarInfoFetchEvent();
            event.setCommand(MAPPER.convertValue(this, JsonNode.class));
            return operatorRepository.find(principal).map(it -> {
                event.fire(it);
                return event;
            });
        }
    }
}
