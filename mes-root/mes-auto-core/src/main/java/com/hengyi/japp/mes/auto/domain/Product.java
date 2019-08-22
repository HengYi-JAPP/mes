package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;

/**
 * 产品
 *
 * @author jzb 2018-06-21
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class Product extends LoggableMongoEntity {
    @ToString.Include
    private String name;
    @ToString.Include
    private String code;
    private boolean canToDty;

    @JsonIgnore
    private Collection<SilkException> dyeingExceptions;
    @JsonIgnore
    private Collection<SilkNote> dyeingNotes;
    @JsonIgnore
    private FormConfig dyeingFormConfig;
}
