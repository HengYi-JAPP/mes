package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.PackageBoxFlip;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.dto.EntityByCodeDTO;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.dto.SilkCarRecordDTO;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.PackageBoxFlipRepository;
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
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PackageBoxFlipEvent extends EventSource {
    private PackageBoxFlip packageBoxFlip;
    private JsonNode command;

    @Override
    public Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        final Collection<Silk> silks = packageBoxFlip.getOutSilks();
        return J.emptyIfNull(data).stream()
                .filter(it -> !silks.contains(it.getSilk()))
                .collect(Collectors.toList());
    }

    @Override
    protected Completable _undo(Operator operator) {
        // todo 打包暂时不支持撤销
        throw new IllegalAccessError();
    }

    @Override
    public EventSourceType getType() {
        return EventSourceType.PackageBoxFlipEvent;
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
        private EntityDTO packageBoxFlip;
        private JsonNode command;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<PackageBoxFlipEvent> toEvent() {
            final PackageBoxFlipRepository packageBoxFlipRepository = Jvertx.getProxy(PackageBoxFlipRepository.class);

            final PackageBoxFlipEvent event = new PackageBoxFlipEvent();
            event.setCommand(command);
            return packageBoxFlipRepository.find(packageBoxFlip.getId()).flatMap(packageBoxFlip -> {
                event.setPackageBoxFlip(packageBoxFlip);
                return toEvent(event);
            });
        }
    }

    @Data
    public static class AutoCommand implements Serializable {
        @NotNull
        private EntityByCodeDTO packageBox;
        @NotNull
        @Size(min = 1)
        private List<EntityByCodeDTO> inSilks;
        @NotNull
        @Size(min = 1)
        private List<Item> items;

        public Single<PackageBoxFlipEvent> toEvent(Principal principal) {
            final OperatorRepository operatorRepository = Jvertx.getProxy(OperatorRepository.class);

            PackageBoxFlipEvent event = new PackageBoxFlipEvent();
            event.setCommand(MAPPER.convertValue(this, JsonNode.class));
            return operatorRepository.find(principal).map(operator -> {
                event.fire(operator);
                return event;
            });
        }
    }

    @Data
    public static class WarehouseCommand implements Serializable {
        @NotNull
        private EntityByCodeDTO packageBox;
        @NotNull
        @Size(min = 1)
        private List<EntityByCodeDTO> inSilks;
        @NotNull
        @Size(min = 1)
        private List<Item> items;
    }

    @Data
    public static class Item implements Serializable {
        @NotNull
        private SilkCarRecordDTO silkCarRecord;
        @NotNull
        @Size(min = 1)
        private List<EntityDTO> silks;
    }

}
