package lock;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.domain.SilkBarcode;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import init.UserImport;
import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-03-03
 */
public class LockTest {
    private static final String baseUrl = "http://10.2.0.215:9998";
    private static final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEiLCJpYXQiOjE1NDcxMjk4MzEsImlzcyI6ImphcHAtbWVzLWF1dG8iLCJzdWIiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEifQ.gO_IM7drZHaEn00kJ2a0kne3B3QrR7bcHVA5fI6ReWElMm2bOjatKogDQfYBs6l31uGTQqSzvGegtmgRsW_BRggUIwRgUEJJ99w1arueAQ_2TJQsIgFnNUoQri3uxrqxv039rKthgmwRmRVMqteJO0k-jZj9RfLARXHzqMPtmlb1j8ZQokrsTGCgouYC0uN1pq2ZhN2MYC3kPty_Rpabgq8RWmLqGAIc6436Lg9d-yEAm_UCYZcuisbjbepCNAUD3frq6qrlhRU8o8vhzYZhxoue7TI4QS-PEk0_crEK_H-Sofc9yoQqUsy9jLp2y2yHQvgpTi5ykveu2jIlitA51g";

    public static void main(String[] args) {
        prepareData();
//        fillData1();
    }

    private static void prepareData() {
        final List<SilkBarcode> silkBarcodes = silkBarcodes();
        final Map<String, List<PackageBoxInfo>> map = silkBarcodes.parallelStream()
                .flatMap(silkBarcode -> {
                    final String code = silkBarcode.getCode();
                    return Stream.of(code + "01B", code + "24B").parallel()
                            .map(LockTest::silk)
                            .filter(Objects::nonNull)
                            .map(silk -> {
                                final PackageBoxInfo packageBoxInfo = new PackageBoxInfo();
                                packageBoxInfo.setSilkBarcode(silkBarcode);
                                packageBoxInfo.setSilk(silk);
                                return packageBoxInfo;
                            });
                })
                .peek(LockTest::fillData)
                .collect(Collectors.groupingBy(it -> {
                    final PackageBox packageBox = it.getPackageBox();
                    if (packageBox != null) {
                        // 直接可以翻包
                        return "0";
                    }
                    final SilkCarRecord silkCarRecord = it.getSilkCarRecord();
                    if (silkCarRecord != null) {
                        final int size = it.getSilkCarRecords().size();
                        if (size == 1) {
                            // 人工最多几包进行翻包
                            return "1";
                        }
//                        return "1+";
                        return "1";
                    }
                    // 找那天的补充唛头
                    return "3";
                }));
        print0(map.get("0"));
//        print1(map.get("1"));
    }

    private static void print1(List<PackageBoxInfo> packageBoxInfos) {
        packageBoxInfos.stream().forEach(packageBoxInfo -> {
            final Silk silk = packageBoxInfo.getSilk();
            final SilkCarRecord silkCarRecord = packageBoxInfo.getSilkCarRecord();
            final SilkBarcode silkBarcode = packageBoxInfo.getSilkBarcode();
            final LocalDate ld = J.localDate(silkBarcode.getCodeDate());
            final String join = String.join("\t", "" + ld, silkBarcode.getDoffingNum(), silk.getCode(), "" + silkCarRecord.getId());
            System.out.println(join);
        });
    }

    private static void print0(List<PackageBoxInfo> packageBoxInfos) {
        packageBoxInfos.stream().forEach(packageBoxInfo -> {
            final Silk silk = packageBoxInfo.getSilk();
            final PackageBox packageBox = packageBoxInfo.getPackageBox();
            final SilkBarcode silkBarcode = packageBoxInfo.getSilkBarcode();
            final LocalDate ld = J.localDate(silkBarcode.getCodeDate());
            final String code = packageBox.getCode();
            final String join = String.join("\t", "" + ld, silkBarcode.getDoffingNum(), silk.getCode(), "" + silk.getSpindle(), code);
            System.out.println(join);
        });
    }

    @SneakyThrows
    private static void fillData(PackageBoxInfo packageBoxInfo) {
        final Silk silk = packageBoxInfo.getSilk();
        if (silk.getPackageDateTime() != null) {
            final String urlTpl = baseUrl + "/api/silks/" + silk.getId() + "/packageBox";
            final Map<String, String> map = ImmutableMap.of();
            final Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + token)
                    .url(J.strTpl(urlTpl, map))
                    .build();
            @Cleanup final Response response = UserImport.client.newCall(request).execute();
            @Cleanup final ResponseBody body = response.body();
            @Cleanup final InputStream is = body.byteStream();
            final PackageBox packageBox = MAPPER.readValue(is, PackageBox.class);
            packageBoxInfo.setPackageBox(packageBox);
            return;
        }
        final String urlTpl = baseUrl + "/api/silks/" + silk.getId() + "/silkCarRecords";
        final Map<String, String> map = ImmutableMap.of();
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(J.strTpl(urlTpl, map))
                .build();
        @Cleanup final Response response = UserImport.client.newCall(request).execute();
        @Cleanup final ResponseBody body = response.body();
        @Cleanup final InputStream is = body.byteStream();
        final JsonNode jsonNode = MAPPER.readTree(is);
        if (!response.isSuccessful()) {
            System.out.println(jsonNode);
            return;
        }
        final List<SilkCarRecord> silkCarRecords = Lists.newArrayList();
        for (JsonNode node : jsonNode) {
            final SilkCarRecord silkCarRecord = MAPPER.convertValue(node, SilkCarRecord.class);
            silkCarRecords.add(silkCarRecord);
            packageBoxInfo.setSilkCarRecord(silkCarRecord);
        }
        packageBoxInfo.setSilkCarRecords(silkCarRecords);
    }

    @SneakyThrows
    private static PackageBoxInfo packageBoxInfo(Silk silk) {
        final PackageBoxInfo packageBoxInfo = new PackageBoxInfo();
        if (silk.getPackageDateTime() != null) {
            final String urlTpl = baseUrl + "/api/silks/" + silk.getId() + "/packageBox";
            final Map<String, String> map = ImmutableMap.of();
            final Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + token)
                    .url(J.strTpl(urlTpl, map))
                    .build();
            @Cleanup final Response response = UserImport.client.newCall(request).execute();
            @Cleanup final ResponseBody body = response.body();
            @Cleanup final InputStream is = body.byteStream();
            final PackageBox packageBox = MAPPER.readValue(is, PackageBox.class);
            packageBoxInfo.setPackageBox(packageBox);
        } else {
            final String urlTpl = baseUrl + "/api/silks/" + silk.getId() + "/silkCarRecords";
            final Map<String, String> map = ImmutableMap.of();
            final Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + token)
                    .url(J.strTpl(urlTpl, map))
                    .build();
            @Cleanup final Response response = UserImport.client.newCall(request).execute();
            @Cleanup final ResponseBody body = response.body();
            @Cleanup final InputStream is = body.byteStream();
            final List<SilkCarRecord> silkCarRecords = Lists.newArrayList();
            for (JsonNode node : MAPPER.readTree(is)) {
                final SilkCarRecord silkCarRecord = MAPPER.convertValue(node, SilkCarRecord.class);
                silkCarRecords.add(silkCarRecord);
                packageBoxInfo.setSilkCarRecord(silkCarRecord);
            }
            packageBoxInfo.setSilkCarRecords(silkCarRecords);
        }
        return packageBoxInfo;
    }

    @SneakyThrows
    public static Silk silk(String code) {
        final String urlTpl = baseUrl + "/api/silks/codes/" + code;
        final Map<String, String> map = ImmutableMap.of();
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(J.strTpl(urlTpl, map))
                .build();
        @Cleanup final Response response = UserImport.client.newCall(request).execute();
        @Cleanup final ResponseBody body = response.body();
        @Cleanup final InputStream is = body.byteStream();
        final JsonNode node = MAPPER.readTree(is);
        if (!response.isSuccessful()) {
            final String errorMessage = node.get("errorMessage").asText();
            if (errorMessage.contains("The MaybeSource is empty")) {
                return null;
            }
            throw new RuntimeException();
        }
        return MAPPER.convertValue(node, Silk.class);
    }

    @SneakyThrows
    public static List<SilkBarcode> silkBarcodes() {
        final String urlTpl = baseUrl + "/api/silkBarcodes?lineMachineId=5bffa63d8857b85a437d21cd&startDate=2018-12-29&endDate=2019-03-01&pageSize=100000";
        final Map<String, String> map = ImmutableMap.of();
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(J.strTpl(urlTpl, map))
                .build();
        @Cleanup final Response response = UserImport.client.newCall(request).execute();
        @Cleanup final ResponseBody body = response.body();
        @Cleanup final InputStream is = body.byteStream();
        final List<SilkBarcode> result = Lists.newArrayList();
        for (JsonNode node : MAPPER.readTree(is).get("silkBarcodes")) {
            final SilkBarcode silkBarcode = MAPPER.convertValue(node, SilkBarcode.class);
            result.add(silkBarcode);
        }
        return result;
    }

    @Data
    public static class PackageBoxInfo {
        private SilkBarcode silkBarcode;
        private Silk silk;
        private PackageBox packageBox;
        private List<PackageBox> packageBoxes;
        private SilkCarRecord silkCarRecord;
        private List<SilkCarRecord> silkCarRecords;
    }
}
