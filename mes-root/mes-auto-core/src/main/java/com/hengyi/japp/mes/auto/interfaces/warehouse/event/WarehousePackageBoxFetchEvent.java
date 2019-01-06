package com.hengyi.japp.mes.auto.interfaces.warehouse.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.PackageBoxRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Collection;

import static com.github.ixtf.japp.core.Constant.MAPPER;


/**
 * @author jzb 2018-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class WarehousePackageBoxFetchEvent extends EventSource {
    private PackageBox packageBox;
    private JsonNode command;

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
        return EventSourceType.WarehousePackageBoxFetchEvent;
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
        private EntityDTO packageBox;
        private JsonNode command;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<WarehousePackageBoxFetchEvent> toEvent() {
            final PackageBoxRepository packageBoxRepository = Jvertx.getProxy(PackageBoxRepository.class);

            final WarehousePackageBoxFetchEvent event = new WarehousePackageBoxFetchEvent();
            event.setCommand(command);
            return packageBoxRepository.find(packageBox.getId()).flatMap(it -> {
                event.setPackageBox(it);
                return toEvent(event);
            });
        }
    }

    /**
     * @author jzb 2018-11-07
     */
    @Data
    public static class Command implements Serializable {
        @NotBlank
        private String code;
    }
}
