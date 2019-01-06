package com.hengyi.japp.mes.auto.application.query;

import com.hengyi.japp.mes.auto.domain.PackageBoxFlip;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxFlipType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collection;

/**
 * @author jzb 2018-07-01
 */
@Builder
public class PackageBoxFlipQuery {
    @Getter
    @Builder.Default
    private final int first = 0;
    @Getter
    @Builder.Default
    private final int pageSize = 50;
    @Getter
    private final String packageBoxId;
    @Getter
    private final String workshopId;
    @Getter
    private final String batchId;
    @Getter
    private final String gradeId;
    @Getter
    private final PackageBoxFlipType packageBoxFlipType;
    @Getter
    private final LocalDate startLd;
    @Getter
    private final LocalDate endLd;

    @Builder
    public static class Result {
        @Getter
        private final int first;
        @Getter
        private final int pageSize;
        @Getter
        private final long count;
        @Getter
        private final Collection<PackageBoxFlip> packageBoxFlips;
    }

}
