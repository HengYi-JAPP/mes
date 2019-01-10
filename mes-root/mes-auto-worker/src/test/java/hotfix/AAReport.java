package hotfix;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.application.report.WorkshopProductPlanReport;
import com.hengyi.japp.mes.auto.domain.*;
import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.collections4.IterableUtils;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static init.UserImport.client;

/**
 * @author jzb 2019-01-08
 */
public class AAReport {
    private static final String baseUrl = "http://10.2.0.215:9998";
    private static final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEiLCJpYXQiOjE1NDcwMTQ2NzMsImV4cCI6MTU0NzA1Nzg3MywiaXNzIjoiamFwcC1tZXMtYXV0byIsInN1YiI6IjVjMDRhMDQ0YzNjYWU4MTNiNTMwY2RkMSJ9.psMnD2Ag5QeGP6xckZyLjo5ZJ401qLCK6E7VfiTuhg_9zVJIKxeZcj-gbHe7nhuq5BoIJ5L0dFf6xe2yc0eZeB7HbqIC3mI2xBl2f1mGO-UrBT8h5-SZAiowFyB94O0KDSOmOf2QNyG3yt_VfitKQPRuympoNoHH9_8-d9DUzf5FUrdi29JzkHZKN6vaAdCfbrkeAZXxGX7rYUZ1MMzn9ePuHn-3JX-twYLTe-ceffL7Ol6d8JE7CQRkHBkeGCHomd6gXFQJ0Ie1M65H3XKu2HXdb6pwCWiFY0hjSF7JfQ-VwuIcBMnhKDFHk_XIprfE8GNfYvaz97ntl0I8p07bhg";
    public static final Grade gradeAA = gradeAA();
    private static final Collection<PackageClass> packageClasses = packageClasses();

    @SneakyThrows
    public static void main(String[] args) {
        final LocalDate startLd = LocalDate.of(2019, 1, 8);
        final LocalDate endLd = LocalDate.of(2019, 1, 9);
        final List<AAReportExcel> excelList = Stream.iterate(startLd, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startLd, endLd) + 1)
                .flatMap(ld -> packageClasses.stream().map(packageClass -> new AAReportExcel(ld, packageClass)))
                .collect(Collectors.toList());
        final ForkJoinTask<Void> task = ForkJoinPool.commonPool().submit(new SingleTask(excelList));
        task.get();

//        final LocalDate ld = LocalDate.of(2019, 1, 7);
//        final PackageClass packageClass = new PackageClass();
//        packageClass.setId("5bfd4a0c67e7ad000188a0d9");
//        final AAReportExcel aaReportExcel = new AAReportExcel(ld, packageClass);
//        aaReportExcel.toExcel();
    }

    @SneakyThrows
    public static WorkshopProductPlanReport workshopProductPlanReport(PackageBox packageBox) {
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(baseUrl + "/api/reports/workshopProductPlanReport")
                .build();
        @Cleanup final Response response = client.newCall(request).execute();
        @Cleanup final ResponseBody body = response.body();
        @Cleanup final InputStream is = body.byteStream();
        return MAPPER.readValue(is, WorkshopProductPlanReport.class);
    }

    @SneakyThrows
    public static List<Silk> packageBoxSilks(PackageBox packageBox) {
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(baseUrl + "/api/packageBoxes/" + packageBox.getId() + "/silks")
                .build();
        @Cleanup final Response response = client.newCall(request).execute();
        @Cleanup final ResponseBody body = response.body();
        @Cleanup final InputStream is = body.byteStream();
        final List<Silk> result = Lists.newArrayList();
        for (JsonNode node : MAPPER.readTree(is)) {
            final Silk silk = MAPPER.convertValue(node, Silk.class);
            result.add(silk);
        }
        return result;
    }

    @SneakyThrows
    public static List<AAReportSilkCarRecord> packageBoxSilkCarRecords(PackageBox packageBox) {
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(baseUrl + "/api/packageBoxes/" + packageBox.getId() + "/silkCarRecords")
                .build();
        @Cleanup final Response response = client.newCall(request).execute();
        final List<AAReportSilkCarRecord> result = Lists.newArrayList();
        if (response.isSuccessful()) {
            @Cleanup final ResponseBody body = response.body();
            @Cleanup final InputStream is = body.byteStream();
            for (JsonNode node : MAPPER.readTree(is)) {
                final AAReportSilkCarRecord silkCarRecord = MAPPER.convertValue(node, AAReportSilkCarRecord.class);
                result.add(silkCarRecord);
            }
        } else {
//            System.out.println("packageBoxSilkCarRecords:\t" + packageBox.getCode());
//            result.add(null);
        }
        return result;
    }

    @SneakyThrows
    private static Grade gradeAA() {
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(baseUrl + "/api/grades")
                .build();
        @Cleanup final Response response = client.newCall(request).execute();
        @Cleanup final ResponseBody body = response.body();
        @Cleanup final InputStream is = body.byteStream();
        for (JsonNode node : MAPPER.readTree(is)) {
            final Grade grade = MAPPER.convertValue(node, Grade.class);
            if ("AA".equals(grade.getName())) {
                return grade;
            }
        }
        throw new RuntimeException();
    }

    @SneakyThrows
    private static Collection<PackageClass> packageClasses() {
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(baseUrl + "/api/packageClasses")
                .build();
        @Cleanup final Response response = client.newCall(request).execute();
        @Cleanup final ResponseBody body = response.body();
        @Cleanup final InputStream is = body.byteStream();
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

    @Data
    private static class SingleTask extends RecursiveAction {
        private final List<AAReportExcel> excelList;

        @Override
        protected void compute() {
            final int size = excelList.size();
            if (size < 1) {
                return;
            }

            final AAReportExcel excel = IterableUtils.get(excelList, 0);
            if (size == 1) {
                excel.toExcel();
                return;
            }

            final List<AAReportExcel> list1 = Lists.newArrayList(excel);
            final SingleTask task1 = new SingleTask(list1);
            task1.fork();

            final List<AAReportExcel> list2 = excelList.subList(1, size);
            final SingleTask task2 = new SingleTask(list2);
            task2.fork();

            task1.join();
            task2.join();
        }
    }

    @Data
    public static class AAReportSilkCarRecord extends SilkCarRecord {
        private Collection<SilkRuntime> initSilks;
    }
}
