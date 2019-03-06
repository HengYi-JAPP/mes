package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.dto.SilkCarRecordDTO;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SmallPackageBoxEvent extends EventSource {
    private String smallBatchId;
    private int smallPacageBoxCount;
    private JsonNode command;

    @Override
    public Collection<SilkRuntime> _calcSilkRuntimes(Collection<SilkRuntime> data) {
        return data;
    }

    @Override
    protected Completable _undo(Operator operator) {
        // todo 打包暂时不支持撤销
        throw new IllegalAccessError();
    }

    @Override
    public EventSourceType getType() {
        return EventSourceType.SmallPackageBoxEvent;
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
        private String smallBatchId;
        private JsonNode command;

        public static DTO from(JsonNode jsonNode) {
            return MAPPER.convertValue(jsonNode, DTO.class);
        }

        public Single<SmallPackageBoxEvent> toEvent() {
            final SmallPackageBoxEvent event = new SmallPackageBoxEvent();
            event.setCommand(command);
            event.setSmallBatchId(smallBatchId);
            return toEvent(event);
        }
    }

    @Data
    public static class CommandConfig implements Serializable {
        @Min(1)
        private int silkCount;
    }

    @Data
    public class Command implements Serializable {
        @NotNull
        private SilkCarRecordDTO silkCarRecord;
        @NotNull
        private CommandConfig config;
    }

    @Data
    public class BatchCommand implements Serializable {
        @Size(min = 1)
        @NotNull
        private List<SilkCarRecordDTO> silkCarRecords;
        @NotNull
        private CommandConfig config;
    }
}
