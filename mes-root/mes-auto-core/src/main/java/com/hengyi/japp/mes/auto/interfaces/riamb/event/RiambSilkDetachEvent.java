package com.hengyi.japp.mes.auto.interfaces.riamb.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * 丝锭解绑
 *
 * @author jzb 2018-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class RiambSilkDetachEvent extends EventSource {
    private Command command;

    @Override
    protected Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        final Collection<String> codes = Sets.newHashSet(J.emptyIfNull(command.getSilkCodes()));
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
        return EventSourceType.RiambSilkDetachEvent;
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
        private Command command;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<RiambSilkDetachEvent> toEvent() {
            final RiambSilkDetachEvent event = new RiambSilkDetachEvent();
            event.setCommand(command);
            return toEvent(event);
        }
    }

    /**
     * @author jzb 2018-06-22
     */
    @Data
    public static class Command implements Serializable {
        @NotNull
        private SilkCarInfo silkCarInfo;
        @NotNull
        private List<String> silkCodes;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class SilkCarInfo extends EntityDTO {
        @NotBlank
        private String code;
    }
}
