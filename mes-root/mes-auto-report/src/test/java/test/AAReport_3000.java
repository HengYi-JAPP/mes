package test;

import com.google.inject.Guice;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.report.ReportModule;
import com.hengyi.japp.mes.auto.report.application.StatisticReportService;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.StatisticReportDay;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.StatisticReportRange;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.stream.Stream;

import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-05-31
 */
@Slf4j
public class AAReport_3000 {
    private static final Vertx vertx = Vertx.vertx();

    static {
        // sshfs -o allow_other root@10.2.0.215:/data/mes/auto/db /data/mes-3000/auto/db
        System.setProperty("japp.mes.auto.path", "/data/mes-3000/auto");
        INJECTOR = Guice.createInjector(new GuiceModule(vertx), new ReportModule());
    }

    public static void main(String[] args) {
        final StatisticReportService statisticReportService = INJECTOR.getInstance(StatisticReportService.class);
        final long start = System.currentTimeMillis();

//        final Workshop workshop = Workshops.D;
//        final LocalDate startLd = LocalDate.of(2019, 3, 14);
//        final LocalDate endLd = LocalDate.of(2019, 3, 31);
        final Workshop workshop = Workshops.C;
        final LocalDate startLd = LocalDate.of(2019, 7, 1);
        final LocalDate endLd = LocalDate.of(2019, 7, 7);
        final Collection<StatisticReportDay> days = Stream.iterate(startLd, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startLd, endLd) + 1).parallel()
                .map(it -> statisticReportService.generate(workshop.getId(), it).block())
                .collect(toList());
        days.parallelStream().forEach(StatisticReportDay::testXlsx);

        final StatisticReportDay day = IterableUtils.get(days, 0);
        final StatisticReportRange range = new StatisticReportRange(day.getWorkshop(), startLd, endLd, days);
        range.testXlsx();

        System.out.println(Duration.ofMillis(System.currentTimeMillis() - start).toSeconds());
    }

    public static class Workshops {
        public static final Workshop A;
        public static final Workshop B;
        public static final Workshop C;
        public static final Workshop D;
        public static final Workshop F;

        static {
            A = new Workshop();
            A.setId("5c6e63713d0045000136458c");
            A.setName("A");
            B = new Workshop();
            B.setId("5bffa63d8857b85a437d1fc5");
            B.setName("B");
            C = new Workshop();
            C.setId("5c772ecc26e0ff000148c039");
            C.setName("C");
            D = new Workshop();
            D.setId("5c6d5f353d004500015bf451");
            D.setName("D");
            F = new Workshop();
            F.setId("5c8c22cf8070b400017efdbc");
            F.setName("F");
        }
    }
}
