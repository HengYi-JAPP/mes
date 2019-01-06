package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.application.persistence.MongoEntity;
import com.hengyi.japp.mes.auto.domain.data.DyeingType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 待染判
 *
 * @author jzb 2018-08-02
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DyeingPrepare extends MongoEntity {
    @ToString.Include
    private DyeingType type;

    // 一次染判，位与锭交织、多次染判
    private SilkCarRecord silkCarRecord;
    private Collection<Silk> silks;

    // 位与位交织
    private SilkCarRecord silkCarRecord1;
    private Collection<Silk> silks1;
    private SilkCarRecord silkCarRecord2;
    private Collection<Silk> silks2;

    private Operator creator;
    private Date createDateTime;

    private Collection<DyeingResult> dyeingResults;
    private Operator submitter;
    private Date submitDateTime;

    public boolean isSubmitted() {
        return getSubmitDateTime() != null && getSubmitter() != null;
    }

    @JsonIgnore
    public boolean isFirst() {
        return getType() == DyeingType.FIRST;
    }

    @JsonIgnore
    public boolean isCross() {
        return getType() == DyeingType.CROSS_LINEMACHINE_SPINDLE || getType() == DyeingType.CROSS_LINEMACHINE_LINEMACHINE;
    }

    @JsonIgnore
    public boolean isMulti() {
        return getType() == DyeingType.SECOND || getType() == DyeingType.THIRD;
    }

    public Collection<SilkCarRecord> prepareSilkCarRecords() {
        switch (getType()) {
            case CROSS_LINEMACHINE_LINEMACHINE: {
                return Lists.newArrayList(getSilkCarRecord1(), getSilkCarRecord2());
            }
            default: {
                return Lists.newArrayList(getSilkCarRecord());
            }
        }
    }

    public Collection<Silk> prepareSilks() {
        switch (getType()) {
            case CROSS_LINEMACHINE_LINEMACHINE: {
                final Stream<Silk> stream1 = getSilks1().stream();
                final Stream<Silk> stream2 = getSilks2().stream();
                return Stream.concat(stream1, stream2).collect(Collectors.toList());
            }
            default: {
                return getSilks();
            }
        }
    }

}
