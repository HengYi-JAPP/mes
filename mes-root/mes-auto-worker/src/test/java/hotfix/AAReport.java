package hotfix;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.domain.*;
import init.UserImport;
import lombok.Cleanup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-01-08
 */
public class AAReport {
    private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMinutes(1)).build();
    private static final String baseUrl = "http://10.2.0.215:9998";
    private static final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEiLCJpYXQiOjE1NDcxMjk4MzEsImlzcyI6ImphcHAtbWVzLWF1dG8iLCJzdWIiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEifQ.gO_IM7drZHaEn00kJ2a0kne3B3QrR7bcHVA5fI6ReWElMm2bOjatKogDQfYBs6l31uGTQqSzvGegtmgRsW_BRggUIwRgUEJJ99w1arueAQ_2TJQsIgFnNUoQri3uxrqxv039rKthgmwRmRVMqteJO0k-jZj9RfLARXHzqMPtmlb1j8ZQokrsTGCgouYC0uN1pq2ZhN2MYC3kPty_Rpabgq8RWmLqGAIc6436Lg9d-yEAm_UCYZcuisbjbepCNAUD3frq6qrlhRU8o8vhzYZhxoue7TI4QS-PEk0_crEK_H-Sofc9yoQqUsy9jLp2y2yHQvgpTi5ykveu2jIlitA51g";
    public static final Collection<PackageClass> packageClasses = packageClasses();

    @SneakyThrows
    public static void main(String[] args) {
//        final LocalDate startLd = LocalDate.of(2019, 1, 8);
//        final LocalDate endLd = LocalDate.of(2019, 1, 10);
//        final List<AAReportExcel> excelList = Stream.iterate(startLd, d -> d.plusDays(1))
//                .limit(ChronoUnit.DAYS.between(startLd, endLd) + 1)
//                .map(AAReportExcel::new)
//                .peek(ForkJoinPool.commonPool()::invoke)
//                .collect(Collectors.toList());
//        excelList.forEach(AAReportExcel::toExcel);

        final LocalDate ld = LocalDate.of(2019, 1, 13);
        final AAReportExcel aaReportExcel = new AAReportExcel(ld);
        ForkJoinPool.commonPool().submit(aaReportExcel);
        ForkJoinPool.commonPool().invoke(aaReportExcel);
//        aaReportExcel.toExcel();
//        aaReportExcel.printByDay();
        aaReportExcel.printDetail();
    }

    @SneakyThrows
    public static List<Silk> packageBoxSilks(PackageBox packageBox) {
//        final var request = HttpRequest.newBuilder()
//                .header("Authorization", "Bearer " + token)
//                .uri(URI.create(baseUrl + "/api/packageBoxes/" + packageBox.getId() + "/silks"))
//                .build();
//        final var response = client.send(request, BodyHandlers.ofInputStream());
//        System.out.println(response.statusCode());
//        @Cleanup final InputStream is = response.body();
//        final List<Silk> result = Lists.newArrayList();
//        for (JsonNode node : MAPPER.readTree(is)) {
//            final Silk silk = MAPPER.convertValue(node, Silk.class);
//            result.add(silk);
//        }
//        return result;

        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(baseUrl + "/api/packageBoxes/" + packageBox.getId() + "/silks")
                .build();
        @Cleanup final Response response = UserImport.client.newCall(request).execute();
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
        final var request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .uri(URI.create(baseUrl + "/api/packageBoxes/" + packageBox.getId() + "/silkCarRecords"))
                .build();
        final var httpClient = HttpClient.newHttpClient();
        final var httpResponse = httpClient.send(request, BodyHandlers.ofInputStream());
        final List<AAReportSilkCarRecord> result = Lists.newArrayList();
        if (httpResponse.statusCode() == 200) {
            @Cleanup final InputStream is = httpResponse.body();
            for (JsonNode node : MAPPER.readTree(is)) {
                final var silkCarRecord = MAPPER.convertValue(node, AAReportSilkCarRecord.class);
                result.add(silkCarRecord);
            }
        }
        return result;
    }

    @SneakyThrows
    private static Collection<PackageClass> packageClasses() {
        final HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .uri(URI.create(baseUrl + "/api/packageClasses"))
                .build();
        final var response = client.send(request, BodyHandlers.ofInputStream());
        @Cleanup final InputStream is = response.body();
        final Collection<PackageClass> result = Sets.newHashSet();
        for (JsonNode node : MAPPER.readTree(is)) {
            final PackageClass packageClass = MAPPER.convertValue(node, PackageClass.class);
            result.add(packageClass);
        }
        return result;
    }

    @SneakyThrows
    public static List<PackageBox> packageBoxes(LocalDate ld, PackageClass budatClass) {
        final String urlTpl = baseUrl + "/api/packageBoxes?startDate=${ld}&endDate=${ld}&budatClassId=${budatClassId}&pageSize=1000&workshopId=5bffa63d8857b85a437d1fc5&productId=5bffa63c8857b85a437d1f93";
        final Map<String, String> map = ImmutableMap.of("ld", "" + ld, "budatClassId", budatClass.getId());
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
