package test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.inject.Guice;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.report.ReportModule;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.hengyi.japp.mes.auto.report.application.RedisService;
import com.hengyi.japp.mes.auto.report.application.dto.ExceptionRecordReport;
import com.hengyi.japp.mes.auto.report.application.dto.PackageBoxReport_ByOperator;
import com.hengyi.japp.mes.auto.report.application.dto.silk_car_record.SilkCarRecordAggregate;
import io.vertx.reactivex.core.Vertx;
import io.vertx.redis.RedisOptions;
import org.apache.commons.io.FileUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;
import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static com.hengyi.japp.mes.auto.report.Report.JEDIS_POOL;

/**
 * @author jzb 2019-06-04
 */
public class LuceneTest {
    private static final Vertx vertx = Vertx.vertx();
    private static final String[] WORKSHOP_ID = new String[]{"5c877549a3f0a02467a817f0", "5c877549a3f0a02467a817ef", "5cdcb312039aab00016f6544", "5cdcb6f0039aab0001729ef0"};

    static {
        // sshfs -o ro root@10.2.0.215:/data/mes/auto/db /data/mes-3000/auto/db
//        System.setProperty("japp.mes.auto.path", "/data/mes-3000/auto");
        // sshfs -o ro root@10.61.0.15:/data/mes/auto/db /data/mes-9200/auto/db
        System.setProperty("japp.mes.auto.path", "/data/mes-9200/auto");
        INJECTOR = Guice.createInjector(new GuiceModule(vertx), new ReportModule());

        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        final RedisOptions redisOptions = INJECTOR.getInstance(MesAutoConfig.class).getRedisOptions();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        JEDIS_POOL = new JedisPool(poolConfig, redisOptions.getHost(), 6379, 100000);
    }

    public static void main(String[] args) {
//        Arrays.stream(WORKSHOP_ID).forEach(LuceneTest::ExceptionRecordReport);
//        final File dir = FileUtils.getFile("/home/jzb/test/ExceptionRecordReport");
//        PoiTest.ExceptionRecordReport(FileUtils.getFile(dir, "异常确认.xlsx"));
//        System.out.println("ExceptionRecordReport end");

//        Arrays.stream(WORKSHOP_ID).forEach(LuceneTest::PackageBoxReport);
//        final File dir = FileUtils.getFile("/home/jzb/test/PackageBoxReport");
//        PoiTest.PackageBoxReport(FileUtils.getFile(dir, "打包.xlsx"));
//        System.out.println("PackageBoxReport end");

        Arrays.stream(WORKSHOP_ID).forEach(LuceneTest::AccReport);
        System.out.println("InspectionReport end");
//        PoiTest.InspectionReport(new File("/home/jzb/test/InspectionReport/外观.xlsx"));
//        PoiTest.DoffingReport(new File("/home/jzb/test/DoffingReport/落筒.xlsx"));
//        System.out.println("InspectionReport end");
    }

    private static void ExceptionRecordReport(String workshopId) {
        Mono.fromCallable(() -> {
            final LocalDate startLd = LocalDate.of(2019, Month.SEPTEMBER, 1);
            final LocalDate endLd = LocalDate.of(2019, Month.SEPTEMBER, 30);
            final ExceptionRecordReport report = ExceptionRecordReport.create(workshopId, J.date(startLd).getTime(), J.date(endLd).getTime());
            final JsonNode jsonNode = report.toJsonNode();

            final File dir = FileUtils.getFile("/home/jzb/test/ExceptionRecordReport");
            FileUtils.forceMkdir(dir);
            YAML_MAPPER.writeValue(FileUtils.getFile(dir, workshopId + ".yml"), jsonNode);
            return true;
        }).retry(5).block();
    }

    private static void PackageBoxReport(String workshopId) {
        Mono.fromCallable(() -> {
            final LocalDate startLd = LocalDate.of(2019, Month.SEPTEMBER, 1);
            final LocalDate endLd = LocalDate.of(2019, Month.OCTOBER, 1);
            final PackageBoxReport_ByOperator report = PackageBoxReport_ByOperator.create(workshopId, J.date(startLd).getTime(), J.date(endLd).getTime());
            final JsonNode jsonNode = report.toJsonNode();

            final File dir = FileUtils.getFile("/home/jzb/test/PackageBoxReport");
            FileUtils.forceMkdir(dir);
            YAML_MAPPER.writeValue(FileUtils.getFile(dir, workshopId + ".yml"), jsonNode);
            return true;
        }).retry(5).block();
    }

    private static void AccReport(String workshopId) {
        final LocalDate startLd = LocalDate.of(2019, Month.SEPTEMBER, 1);
        final long startDateTime = J.date(startLd).getTime();
        final LocalDate endLd = LocalDate.of(2019, Month.OCTOBER, 1);
        final long endDateTime = J.date(endLd).getTime() - 1;
        final AccReport accReport = new AccReport(workshopId, startDateTime, endDateTime);

        final QueryService queryService = INJECTOR.getInstance(QueryService.class);
        final Collection<String> ids = RedisService.listSilkCarRuntimeSilkCarRecordIds();
        ids.addAll(queryService.querySilkCarRecordIdsByEventSourceCanHappen(workshopId, startDateTime, endDateTime));
        final AccReport report = Flux.fromIterable(J.emptyIfNull(ids))
                .flatMap(SilkCarRecordAggregate::from)
                .filter(it -> Objects.equals(DoffingType.AUTO, it.getDoffingType()) || Objects.equals(DoffingType.MANUAL, it.getDoffingType()))
                .reduce(accReport, (acc, cur) -> acc.collect(cur)).block();
        report.save(workshopId);
    }

//    private static void InspectionReport(String workshopId) {
//        Mono.fromCallable(() -> {
//            final LocalDate startLd = LocalDate.of(2019, Month.SEPTEMBER, 1);
//            final LocalDate endLd = LocalDate.of(2019, Month.OCTOBER, 1);
//            final InspectionReport report = InspectionReport.create(workshopId, J.date(startLd).getTime(), J.date(endLd).getTime());
//            final JsonNode jsonNode = report.toJsonNode();
//            MAPPER.writeValue(new File("/home/jzb/test/InspectionReport/" + workshopId + ".json"), jsonNode);
//            return true;
//        }).retry(5).block();
//    }
//
//    @SneakyThrows
//    private static void InspectionReport(String workshopId) {
//        final JsonNode jsonNode = IntStream.rangeClosed(1, 30).mapToObj(it -> LocalDate.of(2019, Month.SEPTEMBER, it))
//                .map(ld -> InspectionReport(workshopId, ld))
//                .reduce(MAPPER.createObjectNode(), (acc, cur) -> {
//                    for (JsonNode jsonNode : cur) {
//
//                    }
//                    return acc;
//                });
//        MAPPER.writeValue(new File("/home/jzb/test/InspectionReport/" + workshopId + ".json"), jsonNode);
//    }
//
//    private static JsonNode InspectionReport(String workshopId, LocalDate ld) {
//        return Mono.fromCallable(() -> {
//            final InspectionReport report = InspectionReport.create(workshopId, J.date(ld).getTime(), J.date(ld.plusDays(1)).getTime() - 1);
//            return report.toJsonNode();
//        }).retry(5).block();
//    }
}
