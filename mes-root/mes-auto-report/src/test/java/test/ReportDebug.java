package test;

import com.google.inject.Guice;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.report.ReportModule;
import com.hengyi.japp.mes.auto.report.application.dto.inspection.InspectionReport;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;

/**
 * @author jzb 2019-05-31
 */
@Slf4j
public class ReportDebug {
    private static final Vertx vertx = Vertx.vertx();

    public static void main(String[] args) {
        // sshfs -o allow_other root@10.61.0.15:/data/mes/auto/db /data/mes-9200/auto/db
        System.setProperty("japp.mes.auto.path", "/data/mes-9200/auto");
        INJECTOR = Guice.createInjector(new GuiceModule(vertx), new ReportModule());
        final InspectionReport report = InspectionReport.create("5c877549a3f0a02467a817ef", 1579392000000l, 1579420800000l);
        System.out.println(report);
//        final long startL = J.date(LocalDate.of(2019, 11, 10)).getTime();
//        final long endL = J.date(LocalDate.of(2019, 11, 11)).getTime();
//        final ExceptionRecordReport exceptionRecordReport = ExceptionRecordReport.create("5c877549a3f0a02467a817f0", 1573084800000l, 1573171200000l);
//        System.out.println(exceptionRecordReport.toJsonNode());
    }

}
