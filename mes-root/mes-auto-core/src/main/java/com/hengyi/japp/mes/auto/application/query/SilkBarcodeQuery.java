package com.hengyi.japp.mes.auto.application.query;

import com.hengyi.japp.mes.auto.domain.SilkBarcode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collection;

/**
 * @author jzb 2018-07-01
 */
@Builder
public class SilkBarcodeQuery {
    @Getter
    @Builder.Default
    private final int first = 0;
    @Getter
    @Builder.Default
    private final int pageSize = 50;
    @Getter
    @Builder.Default
    private final LocalDate startLd = LocalDate.now();
    @Getter
    @Builder.Default
    private final LocalDate endLd = LocalDate.now().plusDays(1);
    @Getter
    private final long codeDoffingNum;
    @Getter
    private final String workshopId;
    @Getter
    private final String lineId;
    @Getter
    private final String lineMachineId;
    @Getter
    private final String batchId;
    @Getter
    private final String doffingNum;
    @Getter
    private final String doffingNumQ;

    @Builder
    public static class Result {
        @Getter
        private final int first;
        @Getter
        private final int pageSize;
        @Getter
        private final long count;
        @Getter
        private final Collection<SilkBarcode> silkBarcodes;
    }

}
