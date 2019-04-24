package com.hengyi.japp.mes.auto.application.query;

import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collection;

/**
 * @author jzb 2018-10-19
 */
@Builder
public class SilkCarRecordByWorkshopQuery {
    @Getter
    @Builder.Default
    private final LocalDate startDate = LocalDate.now();//开始日期
    @Getter
    private final LocalDate endDate;//结束日期
    @Getter
    private final String workshopId;//车间

    @Builder
    public static class Result {
        @Getter
        private final Collection<SilkCarRecord> silkCarRecords;
    }
}
