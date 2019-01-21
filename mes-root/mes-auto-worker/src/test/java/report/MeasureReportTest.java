package report;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-01-15
 */
public class MeasureReportTest {
    private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMinutes(1)).build();
    private static final String baseUrl = "http://10.2.0.215:9998";
    private static final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEiLCJpYXQiOjE1NDcxMjk4MzEsImlzcyI6ImphcHAtbWVzLWF1dG8iLCJzdWIiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEifQ.gO_IM7drZHaEn00kJ2a0kne3B3QrR7bcHVA5fI6ReWElMm2bOjatKogDQfYBs6l31uGTQqSzvGegtmgRsW_BRggUIwRgUEJJ99w1arueAQ_2TJQsIgFnNUoQri3uxrqxv039rKthgmwRmRVMqteJO0k-jZj9RfLARXHzqMPtmlb1j8ZQokrsTGCgouYC0uN1pq2ZhN2MYC3kPty_Rpabgq8RWmLqGAIc6436Lg9d-yEAm_UCYZcuisbjbepCNAUD3frq6qrlhRU8o8vhzYZhxoue7TI4QS-PEk0_crEK_H-Sofc9yoQqUsy9jLp2y2yHQvgpTi5ykveu2jIlitA51g";

    public static void main(String[] args) {
        final LocalDate startLd = LocalDate.of(2019, 1, 14);
        final LocalDate endLd = LocalDate.of(2019, 1, 20);
        final Collection<PackageBox> packageBoxes = report(startLd, endLd);

        printDetail(packageBoxes);
//        printReport(packageBoxes);
    }

//    private static void printReport(Collection<PackageBox> packageBoxes) {
//        final MeasureReport measureReport = new MeasureReport(workshop, ld, budatClass, packageBoxes);
//        measureReport.getItems().forEach(item -> {
//            final Batch batch = item.getBatch();
//            final Grade grade = item.getGrade();
//            final int silkCount = item.getPackageBoxes().parallelStream()
//                    .mapToInt(PackageBox::getSilkCount)
//                    .sum();
//            final double netWeight = item.getPackageBoxes().parallelStream()
//                    .mapToDouble(PackageBox::getNetWeight)
//                    .sum();
//            final BigDecimal bigDecimal = BigDecimal.valueOf(netWeight).setScale(3, RoundingMode.HALF_UP);
//            final String join = String.join("\t", batch.getBatchNo(), grade.getName(), "" + silkCount, "" + bigDecimal);
//            System.out.println(join);
//        });
//    }

    private static void printDetail(Collection<PackageBox> packageBoxes) {
        packageBoxes.forEach(it -> {
            final LocalDate budat = J.localDate(it.getBudat());
            final Batch batch = it.getBatch();
            final Grade grade = it.getGrade();
            final int silkCount = it.getSilkCount();
            final double netWeight = it.getNetWeight();
            final String join = String.join("\t", it.getCode(), "" + budat, batch.getBatchNo(), grade.getName(), "" + silkCount, "" + netWeight);
            System.out.println(join);
        });
    }

    @SneakyThrows
    private static Collection<PackageBox> report(LocalDate startLd, LocalDate endLd) {
        final String urlTpl = "/api/packageBoxes?startDate=${startDate}&endDate=${endDate}&pageSize=10000&workshopId=5bffa63d8857b85a437d1fc5&productId=5bffa63c8857b85a437d1f93";
        final Map<String, String> map = ImmutableMap.of("startDate", "" + startLd, "endDate", "" + endLd);
        final HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .uri(URI.create(baseUrl + J.strTpl(urlTpl, map)))
                .build();
        final var response = client.send(request, BodyHandlers.ofInputStream());
        @Cleanup final InputStream is = response.body();
        final Collection<PackageBox> result = Sets.newHashSet();
        final JsonNode bodyNode = MAPPER.readTree(is);
        for (JsonNode node : bodyNode.get("packageBoxes")) {
            final var packageBox = MAPPER.convertValue(node, PackageBox.class);
            result.add(packageBox);
        }
        return result;
    }
}
