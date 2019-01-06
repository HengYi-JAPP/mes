package com.hengyi.japp.mes.auto.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 表样丝
 *
 * @author jzb 2018-07-29
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DyeingSample extends LoggableMongoEntity {
    @ToString.Include
    private Silk silk;
    @ToString.Include
    private String code;
    private String lineName;
    private int lineMachineItem;
    private int spindle;
    private String batchNo;
    private String doffingNum;
    private boolean used;
}
