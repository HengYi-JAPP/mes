package com.hengyi.japp.mes.auto.application.query;

import com.hengyi.japp.mes.auto.domain.Silk;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collection;

/**
 * @author jzb 2018-07-01
 */
@Builder
public class SilkQuery {
    @Getter
    @Builder.Default
    private final int first = 0;
    @Getter
    @Builder.Default
    private final int pageSize = 50;
    @Getter
    private final String workshopId;
    @Getter
    private final String batchId;
    @Getter
    private final boolean isDyeingSample;
    @Getter
    private final LocalDate ldStart;
    @Getter
    private final LocalDate ldEnd;

    @Builder
    public static class Result {
        @Getter
        private final int first;
        @Getter
        private final int pageSize;
        @Getter
        private final long count;
        @Getter
        private final Collection<Silk> silks;
    }

}
