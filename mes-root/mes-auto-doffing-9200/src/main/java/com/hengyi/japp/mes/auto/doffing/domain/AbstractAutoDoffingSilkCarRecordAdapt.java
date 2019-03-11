package com.hengyi.japp.mes.auto.doffing.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * @author jzb 2019-03-08
 */
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@MappedSuperclass
public abstract class AbstractAutoDoffingSilkCarRecordAdapt implements Serializable {
    @ToString.Include
    @EqualsAndHashCode.Include
    @Id
    private String id;
    @ToString.Include
    private String code;
    @ToString.Include
    private int rowNum;
    @ToString.Include
    private int colNum;
    @ToString.Include
    private String batch;
    private String grade;
    private int state;
    private Long createDateTime;
    private Long modifyDateTime;
    private String silks_A_1_1;
    private String silks_A_1_2;
    private String silks_A_1_3;
    private String silks_A_1_4;
    private String silks_A_1_5;
    private String silks_A_1_6;
    private String silks_A_2_1;
    private String silks_A_2_2;
    private String silks_A_2_3;
    private String silks_A_2_4;
    private String silks_A_2_5;
    private String silks_A_2_6;
    private String silks_A_3_1;
    private String silks_A_3_2;
    private String silks_A_3_3;
    private String silks_A_3_4;
    private String silks_A_3_5;
    private String silks_A_3_6;
    private String silks_A_4_1;
    private String silks_A_4_2;
    private String silks_A_4_3;
    private String silks_A_4_4;
    private String silks_A_4_5;
    private String silks_A_4_6;
    private String silks_B_1_1;
    private String silks_B_1_2;
    private String silks_B_1_3;
    private String silks_B_1_4;
    private String silks_B_1_5;
    private String silks_B_1_6;
    private String silks_B_2_1;
    private String silks_B_2_2;
    private String silks_B_2_3;
    private String silks_B_2_4;
    private String silks_B_2_5;
    private String silks_B_2_6;
    private String silks_B_3_1;
    private String silks_B_3_2;
    private String silks_B_3_3;
    private String silks_B_3_4;
    private String silks_B_3_5;
    private String silks_B_3_6;
    private String silks_B_4_1;
    private String silks_B_4_2;
    private String silks_B_4_3;
    private String silks_B_4_4;
    private String silks_B_4_5;
    private String silks_B_4_6;

}
