package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hengyi.japp.mes.auto.application.persistence.annotations.JsonEntityProperty;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import com.hengyi.japp.mes.auto.interfaces.jackson.LineMachineEmbedSerializer;
import com.hengyi.japp.mes.auto.interfaces.jackson.ProductPlanNotifyEmbedSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * 机台生产计划
 * 对应一个计划通知
 *
 * @author jzb 2018-06-22
 */
@MongoCache
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LineMachineProductPlan extends LoggableMongoEntity {
    @ToString.Include
    @JsonSerialize(using = ProductPlanNotifyEmbedSerializer.class)
    @JsonEntityProperty("notify")
    private ProductPlanNotify productPlanNotify;
    @ToString.Include
    @JsonSerialize(using = LineMachineEmbedSerializer.class)
    @JsonEntityProperty("machine")
    private LineMachine lineMachine;
    @ToString.Include
    private Batch batch;
    private Date startDate;
    private Date endDate;
    /**
     * 指向上一个计划
     */
    @JsonIgnore
    private LineMachineProductPlan prev;
    /**
     * 指向下一个计划
     * 可能会有提前录入计划
     */
    @JsonIgnore
    private LineMachineProductPlan next;

}
