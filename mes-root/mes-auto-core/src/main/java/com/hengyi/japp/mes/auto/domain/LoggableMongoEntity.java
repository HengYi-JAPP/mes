package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hengyi.japp.mes.auto.application.persistence.MongoEntity;
import com.hengyi.japp.mes.auto.application.persistence.annotations.JsonEntityProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * @author jzb 2018-06-22
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public abstract class LoggableMongoEntity extends MongoEntity {
    @JsonIgnore
    private Operator creator;
    @JsonEntityProperty("cdt")
    private Date createDateTime;
    @JsonIgnore
    private Operator modifier;
    @JsonEntityProperty("mdt")
    private Date modifyDateTime;

    public void log(Operator operator) {
        log(operator, new Date());
    }

    public void log(Operator operator, Date date) {
        setModifier(operator);
        setModifyDateTime(date);
        if (getCreator() == null) {
            setCreator(operator);
            setCreateDateTime(date);
        }
    }

}
