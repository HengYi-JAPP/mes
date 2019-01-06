package com.hengyi.japp.mes.auto.domain;

import com.hengyi.japp.mes.auto.application.persistence.annotations.JsonEntityProperty;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;

/**
 * 车间
 *
 * @author jzb 2018-06-21
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class Workshop extends LoggableMongoEntity {
    @JsonEntityProperty("corp")
    private Corporation corporation;
    @ToString.Include
    private String name;
    /**
     * 丝锭条码打印的编码，一位
     */
    @ToString.Include
    private String code;
    private String note;
    private Collection<SapT001l> sapT001ls;
    /**
     * 外贸SAP库存地
     */
    private Collection<SapT001l> sapT001lsForeign;
    /**
     * 塑托SAP库存地
     */
    private Collection<SapT001l> sapT001lsPallet;

}
