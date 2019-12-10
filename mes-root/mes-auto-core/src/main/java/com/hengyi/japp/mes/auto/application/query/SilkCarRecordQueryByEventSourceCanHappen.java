package com.hengyi.japp.mes.auto.application.query;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author jzb 2019-11-13
 */
@Builder
public class SilkCarRecordQueryByEventSourceCanHappen implements Serializable {
    @Getter
    private final String workshopId;
    @Getter
    private final Date startDateTime;
    @Getter
    private final Date endDateTime;
}
