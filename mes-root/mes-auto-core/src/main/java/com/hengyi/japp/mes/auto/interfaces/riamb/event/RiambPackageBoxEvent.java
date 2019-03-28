package com.hengyi.japp.mes.auto.interfaces.riamb.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.application.event.ExceptionCleanEvent;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
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
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class RiambPackageBoxEvent extends EventSource {
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
        return EventSourceType.RiambPackageBoxEvent;
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

        public Single<RiambPackageBoxEvent> toEvent() {
            final RiambPackageBoxEvent event = new RiambPackageBoxEvent();
            return toEvent(event);
        }
    }

    /**
     * @author jzb 2018-06-22
     */
    @Data
    public static class Command implements Serializable {
        @NotBlank
        private String code;
        @NotNull
        private AutomaticPackeJobInfo jobInfo;
        @Min(1)
        @NotNull
        private BigDecimal netWeight;
        @Min(1)
        @NotNull
        private BigDecimal grossWeight;
        @Min(1)
        private int silkCount;
        @NotNull
        private Date createDateTime;
        @Size(min = 1)
        @NotNull
        private List<SilkInfo> silkInfos;
        private String palletCode;
        private String otherInfo;
    }

    @Data
    public static class AutomaticPackeJobInfo implements Serializable {
        @NotBlank
        private String id;
        @NotBlank
        private String automaticPackeLine;
        @NotBlank
        private String batchNo;
        @NotBlank
        private String gradeName;
        private String packageClassNo;
        private String saleType;
        private Date budatDate;
        private String palletType;
        private String packageType;
        private String foamType;
        private int foamNum;
        @NotNull
        private String creatorHrId;
        @NotNull
        private Date createDateTime;
        private String lgort;
        private String otherInfo;
    }

    @Data
    public static class SilkInfo implements Serializable {
        @NotBlank
        private String code;
        private BigDecimal weight;
    }
}
