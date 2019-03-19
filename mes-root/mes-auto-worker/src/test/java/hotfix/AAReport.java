package hotfix;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.application.report.StatisticsReport;
import com.hengyi.japp.mes.auto.application.report.StatisticsReportDay;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.report.PoiUtil;
import lombok.Cleanup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-01-08
 */
@Slf4j
public class AAReport {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.DAYS).readTimeout(1, TimeUnit.DAYS).build();
    private static final String baseUrl = "http://10.2.0.215:9998";
    private static final String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1YjM4NGIyY2Q4NzEyMDY0ZjEwMWUzMWUiLCJ1aWQiOiI1YjM4NGIyY2Q4NzEyMDY0ZjEwMWUzMWUiLCJpc3MiOiJqYXBwLW1lcy1hdXRvIn0.h-CPVnDFw0YyCfm7MIAgXIqTlecAhT5VQe43i5aIUeE";

    @SneakyThrows
    public static void main(String[] args) {
        final RequestBody body = RequestBody.create(JSON, "{}");
        final Request request = new Request.Builder()
                .url("http://192.168.0.249:9997/api/rfcs/ZJAPP_CARGO_1")
                .addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJzYXAtdGVzdCIsImlhdCI6MTU1MjQ1ODQ1NCwiZXhwIjoxNTUyNDY1NjU0LCJpc3MiOiJoZW5neWktZXNiIn0.CtKbR2u3u0ydoWq1FDshqJ013gYV8lR85Nb_QXduYlXRSXfeVqyT1gT6F_cKXlM_WBzAqlLromyEuU2Kte1jrLn7e8TijPRYeLrDObVD5-KDHXJWUqQ74Wx5QDvntsVjCiI6P6F50ezAEl6A1zOcmLvjcPcMYtgQYstN6UWtP_nMzp5IbxHTGP35_m-Fav2UBVQe5DCZGAnuvb7VzV-n0v-zSnvWoNytYvIBosWVKKA2uxoKRxgUViIm3pdTU4qZSqeG6E9cFjvkrizOpJrDBOzFemCYTtH65vAcTr_Sd7LY-b_VsIemB9j_i3HKnihH83yz5NUFoprO0t96amNlyQ")
                .post(body).build();
        final Response response = client.newCall(request).execute();
        System.out.println(response.body().string());

//        final long startL = System.currentTimeMillis();
//
//        final Workshop workshop = Workshops.B;
//        final LocalDate startLd = LocalDate.of(2019, 3, 1);
//        final LocalDate endLd = LocalDate.of(2019, 3, 1);
//        final Collection<StatisticsReportDay> days = Stream.iterate(startLd, d -> d.plusDays(1))
//                .limit(ChronoUnit.DAYS.between(startLd, endLd) + 1).parallel()
//                .map(it -> new AAReportDay(workshop, it)).sorted()
//                .collect(Collectors.toList());
//
//        days.parallelStream().forEach(AAReport::toExcel);
//        toExcel(new StatisticsReport(workshop, startLd, endLd, days));

//        final long endL = System.currentTimeMillis();
//        System.out.println("用时：" + Duration.ofMillis(endL - startL).getSeconds());
    }

    @SneakyThrows
    private static void toExcel(StatisticsReportDay report) {
        @Cleanup final Workbook wb = new XSSFWorkbook();
        final Sheet sheet = wb.createSheet();
        PoiUtil.fillData(wb, sheet, report.getItems());
        @Cleanup final FileOutputStream os = new FileOutputStream("/home/jzb/" + report.getWorkshop().getName() + "." + report.getLd() + ".xlsx");
        wb.write(os);
    }

    @SneakyThrows
    private static void toExcel(StatisticsReport report) {
        @Cleanup final Workbook wb = new XSSFWorkbook();
        final Sheet sheet = wb.createSheet();
        PoiUtil.fillData(wb, sheet, report.getItems());
        @Cleanup final FileOutputStream os = new FileOutputStream("/home/jzb/" + report.getWorkshop().getName() + "." + report.getStartLd() + "~" + report.getEndLd() + ".xlsx");
        wb.write(os);
    }

    @SneakyThrows
    public static List<PackageBox> packageBoxes(Workshop workshop, LocalDate ld) {
        final String urlTpl = baseUrl + "/api/packageBoxes?startDate=${ld}&endDate=${ld}&pageSize=10000&workshopId=${workshopId}";
        final Map<String, String> map = ImmutableMap.of("workshopId", workshop.getId(), "ld", "" + ld);
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(J.strTpl(urlTpl, map))
                .build();
        @Cleanup final Response response = client.newCall(request).execute();
        @Cleanup final ResponseBody body = response.body();
        @Cleanup final InputStream is = body.byteStream();
        final List<PackageBox> result = Lists.newArrayList();
        for (JsonNode node : MAPPER.readTree(is).get("packageBoxes")) {
            final PackageBox packageBox = MAPPER.convertValue(node, PackageBox.class);
            result.add(packageBox);
        }
        return result;
    }


    @SneakyThrows
    public static List<Silk> packageBoxSilks(PackageBox packageBox) {
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(baseUrl + "/api/packageBoxes/" + packageBox.getId() + "/silks")
                .build();
        @Cleanup final Response response = client.newCall(request).execute();
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
        @Cleanup final Response response = client.newCall(request).execute();
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

    public static class Workshops {
        public static final Workshop A;
        public static final Workshop B;

        static {
            A = new Workshop();
            A.setId("5c6e63713d0045000136458c");
            A.setName("A");
            B = new Workshop();
            B.setId("5bffa63d8857b85a437d1fc5");
            B.setName("B");
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class AAReportSilkCarRecord extends SilkCarRecord {
        private Collection<SilkRuntime> initSilks;
    }
}
