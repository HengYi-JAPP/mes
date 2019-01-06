package com.hengyi.japp.mes.auto.domain;

import com.hengyi.japp.mes.auto.application.persistence.MongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * @author jzb 2018-07-29
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SilkDetached extends MongoEntity {
    @ToString.Include
    private Silk silk;
    private Operator detachOperator;
    private Date detachDateTime;

}
