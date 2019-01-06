package com.hengyi.japp.mes.auto.interfaces.jikon.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Sets;
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
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * 丝锭解绑
 *
 * @author jzb 2018-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class JikonAdapterSilkDetachEvent extends EventSource {
    private JsonNode command;

    @Override
    protected Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        final Collection<String> codes = Optional.ofNullable(command.get("spindleCode").asText(null))
                .map(it -> StringUtils.split(it, ","))
                .map(Sets::newHashSet)
                .orElse(Sets.newHashSet());
        return J.emptyIfNull(data).stream()
                .filter(it -> !codes.contains(it.getSilk().getCode()))
                .collect(Collectors.toList());
    }

    @Override
    protected Completable _undo(Operator operator) {
        throw new IllegalAccessError();
    }

    @Override
    public EventSourceType getType() {
        return EventSourceType.JikonAdapterSilkDetachEvent;
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

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<JikonAdapterSilkDetachEvent> toEvent() {
            final JikonAdapterSilkDetachEvent event = new JikonAdapterSilkDetachEvent();
            event.setCommand(command);
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
        /**
         * 丝锭条码
         * （,）逗号分割
         */
        private String spindleCode;

        public Single<JikonAdapterSilkDetachEvent> toEvent(Principal principal) {
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            final JikonAdapterSilkDetachEvent event = new JikonAdapterSilkDetachEvent();
            event.setCommand(MAPPER.convertValue(this, JsonNode.class));
            return operatorRepository.find(principal).map(it -> {
                event.fire(it);
                return event;
            });
        }
    }
}
