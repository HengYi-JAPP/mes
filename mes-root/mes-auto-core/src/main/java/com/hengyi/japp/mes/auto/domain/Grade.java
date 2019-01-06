package com.hengyi.japp.mes.auto.domain;

import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author jzb 2018-06-22
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class Grade extends LoggableMongoEntity {
    @ToString.Include
    private String name;
    private String code;
    private int sortBy;

}
