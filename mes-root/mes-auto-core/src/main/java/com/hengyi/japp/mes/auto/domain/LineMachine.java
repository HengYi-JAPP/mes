package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * 机台、线位，位号
 *
 * @author jzb 2018-06-22
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class LineMachine extends LoggableMongoEntity {
    @ToString.Include
    private Line line;
    /**
     * 机台位号
     */
    @ToString.Include
    private int item;
    /**
     * 锭数
     */
    private int spindleNum;
    /**
     * 落筒的锭位顺序
     */
    @NotNull
    private Collection<Integer> spindleSeq;
    /**
     * 当前正在生产的计划
     */
    @JsonIgnore
    private LineMachineProductPlan productPlan;

}
