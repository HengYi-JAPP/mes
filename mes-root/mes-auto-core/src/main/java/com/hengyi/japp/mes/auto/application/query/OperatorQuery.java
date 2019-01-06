package com.hengyi.japp.mes.auto.application.query;

import com.hengyi.japp.mes.auto.domain.Operator;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;

/**
 * @author jzb 2018-07-01
 */
@Builder
public class OperatorQuery {
    @Getter
    @Builder.Default
    private final int first = 0;
    @Getter
    @Builder.Default
    private final int pageSize = 50;
    @Getter
    private final String q;

    @Builder
    public static class Result {
        @Getter
        private final int first;
        @Getter
        private final int pageSize;
        @Getter
        private final long count;
        @Getter
        private final Collection<Operator> operators;
    }

}
