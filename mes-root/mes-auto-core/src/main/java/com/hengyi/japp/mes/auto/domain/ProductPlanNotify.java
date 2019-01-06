package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hengyi.japp.mes.auto.application.persistence.annotations.JsonEntityProperty;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import com.hengyi.japp.mes.auto.domain.data.ProductPlanType;
import com.hengyi.japp.mes.auto.interfaces.jackson.LineMachineEmbedSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;

/**
 * 生产计划改变通知
 *
 * @author jzb 2018-06-21
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class ProductPlanNotify extends LoggableMongoEntity {
    @ToString.Include
    @NotNull
    private ProductPlanType type;
    @ToString.Include
    @NotBlank
    private String name;
    @NotNull
    private Batch batch;
    @NotNull
    private Date startDate;
    private Date endDate;

    @JsonSerialize(contentUsing = LineMachineEmbedSerializer.class)
    @JsonEntityProperty("machines")
    private Collection<LineMachine> lineMachines;

}
