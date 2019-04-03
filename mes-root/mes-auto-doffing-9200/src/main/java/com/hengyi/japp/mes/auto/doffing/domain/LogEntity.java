package com.hengyi.japp.mes.auto.doffing.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @author jzb 2019-04-03
 */
@Data
@Entity
@Table(name = "T_Log")
public class LogEntity implements Serializable {
    @Id
    private String id;
    private String level;
    private String message;
    private String command;
    private String operatorName;
    private Date createDateTime;
}
