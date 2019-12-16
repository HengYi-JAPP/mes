package com.hengyi.japp.mes.auto.report.application;

import com.google.inject.ImplementedBy;
import com.hengyi.japp.mes.auto.domain.SilkException;
import com.hengyi.japp.mes.auto.report.application.ExceptionRecordByClassReport.SilkExceptionByClassReportServiceImpl;
import com.hengyi.japp.mes.auto.report.application.command.ExceptionRecordByClassReportCommand;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author jzb 2019-12-16
 */
@ImplementedBy(SilkExceptionByClassReportServiceImpl.class)
public interface SilkExceptionByClassReportService {

    Mono<Collection<ReportItem>> report(ExceptionRecordByClassReportCommand command);

    @Data
    class ReportItem implements Serializable {
        private SilkException silkException;
        private Collection<ClassCodeItem> classCodeItems;
    }

    @Data
    class ClassCodeItem implements Serializable {
        private String classCode;
        private int silkCount;
    }

}
