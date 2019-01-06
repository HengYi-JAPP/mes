package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hengyi.japp.mes.auto.domain.data.TemporaryBoxRecordType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;

/**
 * @author jzb 2018-09-18
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class TemporaryBoxRecord extends LoggableMongoEntity {
    @ToString.Include
    private TemporaryBox temporaryBox;
    @ToString.Include
    private TemporaryBoxRecordType type;
    private int count;

    @JsonIgnore
    private SilkCarRecord silkCarRecord;
    @JsonIgnore
    private Collection<Silk> silks;

    private PackageBox packageBox;
}
