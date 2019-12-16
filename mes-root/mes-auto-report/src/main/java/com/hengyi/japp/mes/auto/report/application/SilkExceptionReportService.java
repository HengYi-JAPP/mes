package com.hengyi.japp.mes.auto.report.application;

import com.google.inject.ImplementedBy;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.SilkCarRecordAggregateType;
import com.hengyi.japp.mes.auto.report.application.SilkExceptionReport.SilkExceptionReportServiceImpl;
import com.hengyi.japp.mes.auto.report.application.command.ExceptionRecordReportCommand;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * @author jzb 2019-12-16
 */
@ImplementedBy(SilkExceptionReportServiceImpl.class)
public interface SilkExceptionReportService {

    Mono<Collection<ReportItem>> report(ExceptionRecordReportCommand command);

    @Data
    class ReportItem implements Serializable {
        private Line line;
        private Batch batch;
        private int silkCount;
        private Collection<SilkExceptionItem> silkExceptionItems;
        private Collection<GradeItem> gradeItems;
        private NoGradeInfo noGradeInfo;
    }

    @Data
    class SilkExceptionItem implements Serializable {
        private SilkException silkException;
        private int silkCount;
    }

    @Data
    class GradeItem implements Serializable {
        private Grade grade;
        private int silkCount;
    }

    @Data
    class NoGradeInfo implements Serializable {
        private Grade grade;
        private int silkCount;
        private Collection<SilkCarRecordInfo> silkCarRecordInfos;
    }

    @Data
    class SilkCarRecordInfo implements Serializable {
        private String id;
        private SilkCarRecordAggregateType type;
        private SilkCar silkCar;
        private Date startDateTime;
        private Operator creator;
    }
}
