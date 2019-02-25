package com.hengyi.japp.mes.auto.application.query;

import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxType;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;
import java.util.Set;

/**
 * @author jzb 2018-07-01
 */
@Builder
public class PackageBoxQuery {
    @Getter
    @Builder.Default
    private final int first = 0;
    @Getter
    @Builder.Default
    private final int pageSize = 50;
    @Getter
    private final String packageBoxCode;
    @Getter
    private final String creatorId;
    @Getter
    private final String workshopId;
    @Getter
    private final Set<String> budatClassIds;
    @Getter
    private final String batchId;
    @Getter
    private final String productId;
    @Getter
    private final String gradeId;
    @Getter
    private final LocalDateRange budatRange;
    @Getter
    private final PackageBoxType type;

    @Builder
    public static class Result {
        @Getter
        private final int first;
        @Getter
        private final int pageSize;
        @Getter
        private final long count;
        @Getter
        private final Collection<PackageBox> packageBoxes;
    }

}
