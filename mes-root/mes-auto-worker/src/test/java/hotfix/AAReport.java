package hotfix;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.application.report.StatisticsReport;
import com.hengyi.japp.mes.auto.application.report.StatisticsReportDay;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.report.PoiUtil;
import init.UserImport;
import lombok.Cleanup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-01-08
 */
@Slf4j
public class AAReport {
    private static final String baseUrl = "http://10.2.0.215:9998";
    private static final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEiLCJpYXQiOjE1NDcxMjk4MzEsImlzcyI6ImphcHAtbWVzLWF1dG8iLCJzdWIiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEifQ.gO_IM7drZHaEn00kJ2a0kne3B3QrR7bcHVA5fI6ReWElMm2bOjatKogDQfYBs6l31uGTQqSzvGegtmgRsW_BRggUIwRgUEJJ99w1arueAQ_2TJQsIgFnNUoQri3uxrqxv039rKthgmwRmRVMqteJO0k-jZj9RfLARXHzqMPtmlb1j8ZQokrsTGCgouYC0uN1pq2ZhN2MYC3kPty_Rpabgq8RWmLqGAIc6436Lg9d-yEAm_UCYZcuisbjbepCNAUD3frq6qrlhRU8o8vhzYZhxoue7TI4QS-PEk0_crEK_H-Sofc9yoQqUsy9jLp2y2yHQvgpTi5ykveu2jIlitA51g";

    @SneakyThrows
    public static void main(String[] args) {
        final long startL = System.currentTimeMillis();

        final LocalDate startLd = LocalDate.of(2019, 2, 23);
        final LocalDate endLd = LocalDate.of(2019, 2, 23);
        final Collection<StatisticsReportDay> days = Stream.iterate(startLd, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startLd, endLd) + 1).parallel()
                .map(AAReportDay::new).sorted()
                .collect(Collectors.toList());

        days.parallelStream().forEach(AAReport::toExcel);
//        toExcel(new StatisticsReport(null, startLd, endLd, days));

        final long endL = System.currentTimeMillis();
        System.out.println("用时：" + Duration.ofMillis(endL - startL).getSeconds());
    }

    @SneakyThrows
    private static void toExcel(StatisticsReportDay report) {
        @Cleanup final Workbook wb = new XSSFWorkbook();
        final Sheet sheet = wb.createSheet();
        PoiUtil.fillData(wb, sheet, report.getItems());
        @Cleanup final FileOutputStream os = new FileOutputStream("/home/jzb/" + report.getLd() + ".xlsx");
        wb.write(os);
    }

    @SneakyThrows
    private static void toExcel(StatisticsReport report) {
        @Cleanup final Workbook wb = new XSSFWorkbook();
        final Sheet sheet = wb.createSheet();
        PoiUtil.fillData(wb, sheet, report.getItems());
        @Cleanup final FileOutputStream os = new FileOutputStream("/home/jzb/" + report.getStartLd() + "~" + report.getEndLd() + ".xlsx");
        wb.write(os);
    }

    @SneakyThrows
    public static List<Silk> packageBoxSilks(PackageBox packageBox) {
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(baseUrl + "/api/packageBoxes/" + packageBox.getId() + "/silks")
                .build();
        @Cleanup final Response response = UserImport.client.newCall(request).execute();
        if (!response.isSuccessful()) {
            return Collections.EMPTY_LIST;
        }
        @Cleanup final ResponseBody body = response.body();
        @Cleanup final InputStream is = body.byteStream();
        final List<Silk> result = Lists.newArrayList();
        for (JsonNode node : MAPPER.readTree(is)) {
            final var silk = MAPPER.convertValue(node, Silk.class);
            result.add(silk);
        }
        return result;
    }

    @SneakyThrows
    public static List<AAReportSilkCarRecord> packageBoxSilkCarRecords(PackageBox packageBox) {
        final var request = new Request.Builder()
                .header("Authorization", "Bearer " + token)
                .url(baseUrl + "/api/packageBoxes/" + packageBox.getId() + "/silkCarRecords")
                .build();
        @Cleanup final Response response = UserImport.client.newCall(request).execute();
        if (!response.isSuccessful()) {
            return Collections.EMPTY_LIST;
        }
        final List<AAReportSilkCarRecord> result = Lists.newArrayList();
        @Cleanup final ResponseBody body = response.body();
        @Cleanup final InputStream is = body.byteStream();
        for (JsonNode node : MAPPER.readTree(is)) {
            final var silkCarRecord = MAPPER.convertValue(node, AAReportSilkCarRecord.class);
            result.add(silkCarRecord);
        }
        return result;
    }

    @SneakyThrows
    public static List<PackageBox> packageBoxes(LocalDate ld) {
        final String urlTpl = baseUrl + "/api/packageBoxes?startDate=${ld}&endDate=${ld}&pageSize=10000&workshopId=5bffa63d8857b85a437d1fc5&productId=5bffa63c8857b85a437d1f93";
        final Map<String, String> map = ImmutableMap.of("ld", "" + ld);
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(J.strTpl(urlTpl, map))
                .build();
        @Cleanup final Response response = UserImport.client.newCall(request).execute();
        @Cleanup final ResponseBody body = response.body();
        @Cleanup final InputStream is = body.byteStream();
        final List<PackageBox> result = Lists.newArrayList();
        for (JsonNode node : MAPPER.readTree(is).get("packageBoxes")) {
            final PackageBox packageBox = MAPPER.convertValue(node, PackageBox.class);
            result.add(packageBox);
        }
        return result;
    }

    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class AAReportSilkCarRecord extends SilkCarRecord {
        private Collection<SilkRuntime> initSilks;
    }
}
