package test;

import com.hengyi.japp.mes.auto.report.Report;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jzb 2019-05-31
 */
@Slf4j
public class ReportDebug {
    private static final Vertx vertx = Vertx.vertx();

    public static void main(String[] args) {
        System.setProperty("japp.mes.auto.path", "/data/mes-9200/auto");
        Report.main(null);
//        INJECTOR = Guice.createInjector(new GuiceModule(vertx), new ReportModule());
//        final long startL = J.date(LocalDate.of(2019, 11, 10)).getTime();
//        final long endL = J.date(LocalDate.of(2019, 11, 11)).getTime();
//        final ExceptionRecordReport exceptionRecordReport = ExceptionRecordReport.create("5c877549a3f0a02467a817f0", 1573084800000l, 1573171200000l);
//        System.out.println(exceptionRecordReport.toJsonNode());
    }

}
