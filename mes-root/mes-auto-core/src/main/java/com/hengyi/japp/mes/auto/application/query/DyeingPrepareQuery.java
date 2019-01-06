package com.hengyi.japp.mes.auto.application.query;

import com.hengyi.japp.mes.auto.domain.DyeingPrepare;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;

/**
 * @author jzb 2018-07-01
 */
@Builder
public class DyeingPrepareQuery {
    @Getter
    @Builder.Default
    private final int first = 0;
    @Getter
    @Builder.Default
    private final int pageSize = 50;
    @Getter
    private final boolean submitted;
    @Getter
    private final String workshopId;
    @Getter
    private final String silkCarId;
    @Getter
    private final String hrIdQ;
    @Getter
    private final long startDateTimestamp;
    @Getter
    private final long endDateTimestamp;
    @Getter
    private final String lineMachineId;
    @Getter
    private final String doffingNum;
    /**
     * FIRST
     * CROSS
     */
    @Getter
    private final String typeString;

    @Builder
    public static class Result {
        @Getter
        private final int first;
        @Getter
        private final int pageSize;
        @Getter
        private final long count;
        @Getter
        private final Collection<DyeingPrepare> dyeingPrepares;
    }
}
