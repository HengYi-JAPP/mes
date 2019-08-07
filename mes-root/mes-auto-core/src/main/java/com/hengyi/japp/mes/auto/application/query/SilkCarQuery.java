package com.hengyi.japp.mes.auto.application.query;

import com.hengyi.japp.mes.auto.domain.SilkCar;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;

/**
 * @author jzb 2018-07-01
 */
@Builder
public class SilkCarQuery {
    @Getter
    @Builder.Default
    private final int first = 0;
    @Getter
    @Builder.Default
    private final int pageSize = 50;
    @Getter
    private final String q;
    @Getter
    private final String type;
    @Getter
    private final int row;
    @Getter
    private final int col;

    @Builder
    public static class Result {
        @Getter
        private final int first;
        @Getter
        private final int pageSize;
        @Getter
        private final long count;
        @Getter
        private final Collection<SilkCar> silkCars;
    }

}
