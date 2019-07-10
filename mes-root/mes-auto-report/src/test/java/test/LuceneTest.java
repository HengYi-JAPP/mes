package test;

import com.google.inject.Guice;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.report.ReportModule;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.hengyi.japp.mes.auto.report.application.StatisticReportService;
import io.vertx.reactivex.core.Vertx;
import org.bson.Document;
import reactor.core.publisher.Flux;
import test.AAReport_3000.Workshops;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-06-04
 */
public class LuceneTest {
    private static final Vertx vertx = Vertx.vertx();

    static {
        // sshfs -o allow_other root@10.2.0.215:/data/mes/auto/db /data/mes-3000/auto/db
        System.setProperty("japp.mes.auto.path", "/data/mes-3000/auto");
        INJECTOR = Guice.createInjector(new GuiceModule(vertx), new ReportModule());
    }

    public static void main(String[] args) {
        final StatisticReportService statisticReportService = INJECTOR.getInstance(StatisticReportService.class);
        final Collection<String> ids = statisticReportService.packageBoxIds(Workshops.C.getId(), LocalDate.of(2019, 7, 7));

        final List<Document> docs = Flux.fromIterable(ids)
                .flatMap(id -> QueryService.find(PackageBox.class, id))
                .filter(document -> {
                    final Double netWeight = document.getDouble("netWeight");
                    return document.getString("code").contains("GC021511")
                            && "FOREIGN".equals(document.getString("saleType"))
                            && netWeight.intValue() == 549;
                })
                .collectList().block();
        System.out.println(docs.size());
        final List<String> codes = docs.stream().map(document -> document.getString("code")).collect(toList());
        Collections.sort(codes);
        codes.forEach(System.out::println);
    }
}
