package com.hengyi.japp.mes.auto.domain;

import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.Min;

/**
 * @author jzb 2018-09-18
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache()
public class TemporaryBox extends LoggableMongoEntity {
    @ToString.Include
    private String code;
    @ToString.Include
    private Batch batch;
    @ToString.Include
    private Grade grade;
    @Min(0)
    private int count;
}
