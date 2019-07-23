package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hengyi.japp.mes.auto.application.persistence.MongoEntity;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.Date;

/**
 * @author jzb 2018-07-29
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Silk extends MongoEntity {
    @ToString.Include
    private String code;
    /**
     * 落次
     */
    @ToString.Include
    private String doffingNum;
    private Operator doffingOperator;
    private DoffingType doffingType;
    private Date doffingDateTime;
    @ToString.Include
    private LineMachine lineMachine;
    @ToString.Include
    private int spindle;
    private Batch batch;
    private Grade grade;
    /**
     * 外观确认的最终异常
     */
    private SilkException exception;
    private Collection<SilkException> exceptions;
    private Collection<String> dyeingExceptionStrings;

    /**
     * 是否是染判标样丝，
     * 标样丝，不计入产量
     */
    private boolean dyeingSample;
    private boolean detached;

    /**
     * 丝锭经历过的车次
     */
    @JsonIgnore
    private Collection<SilkCarRecord> silkCarRecords;

    @JsonIgnore
    private TemporaryBox temporaryBox;
    @JsonIgnore
    private PackageBox packageBox;
    private Date packageDateTime;
    // 外观称重
    private double weight;

}
