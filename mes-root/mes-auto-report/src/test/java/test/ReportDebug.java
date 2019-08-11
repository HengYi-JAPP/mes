package test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxType;
import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import test.AAReport_3000.Workshops;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.github.ixtf.japp.poi.Jpoi.cell;
import static com.hengyi.japp.mes.auto.report.application.dto.statistic.PoiUtil.addHeads;
import static org.apache.poi.ss.util.CellUtil.getRow;

/**
 * @author jzb 2019-05-31
 */
@Slf4j
public class ReportDebug {
    private static final OkHttpClient okHttpClient = new OkHttpClient();
    private static final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEiLCJpYXQiOjE1NDcxMjk4MzEsImlzcyI6ImphcHAtbWVzLWF1dG8iLCJzdWIiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEifQ.gO_IM7drZHaEn00kJ2a0kne3B3QrR7bcHVA5fI6ReWElMm2bOjatKogDQfYBs6l31uGTQqSzvGegtmgRsW_BRggUIwRgUEJJ99w1arueAQ_2TJQsIgFnNUoQri3uxrqxv039rKthgmwRmRVMqteJO0k-jZj9RfLARXHzqMPtmlb1j8ZQokrsTGCgouYC0uN1pq2ZhN2MYC3kPty_Rpabgq8RWmLqGAIc6436Lg9d-yEAm_UCYZcuisbjbepCNAUD3frq6qrlhRU8o8vhzYZhxoue7TI4QS-PEk0_crEK_H-Sofc9yoQqUsy9jLp2y2yHQvgpTi5ykveu2jIlitA51g";

    @SneakyThrows
    public static void main(String[] args) {
        final LocalDate startLd = LocalDate.of(2019, Month.JANUARY, 1);
//        final LocalDate endLd = LocalDate.of(2019, Month.JULY, 1);
        final LocalDate endLd = LocalDate.of(2019, Month.JULY, 31);
        final Map<CollectKey, CollectValue> map = Maps.newConcurrentMap();
        Stream.iterate(startLd, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startLd, endLd) + 1)
                .map(ld -> packageBoxes(Workshops.B, ld))
                .flatMap(Collection::stream)
                .forEach(it -> collectMap(map, it));

        @Cleanup final Workbook wb = new XSSFWorkbook();
        fillSheet(wb.createSheet(), map);
        @Cleanup final FileOutputStream os = new FileOutputStream("/home/jzb/test.xlsx");
        wb.write(os);
    }

    private static void fillSheet(Sheet sheet, Map<CollectKey, CollectValue> map) {
        final String[] heads = {"批号", "等级", "打包方式", "筒管数", "净重"};
        Row row = addHeads(sheet, heads);
        int rowIndex = 0;
        Cell cell = null;
        for (Map.Entry<CollectKey, CollectValue> entry : map.entrySet()) {
            final CollectKey collectKey = entry.getKey();
            final Batch batch = collectKey.getBatch();
            final Grade grade = collectKey.getGrade();
            final PackageBoxType packageBoxType = collectKey.getPackageBoxType();
            if (packageBoxType == PackageBoxType.BIG_SILK_CAR
                    || packageBoxType == PackageBoxType.AUTO
                    || grade.getSortBy() < 100) {
                continue;
            }
            final CollectValue collectValue = entry.getValue();
            row = getRow(++rowIndex, sheet);
            cell = cell(row, 'A');
            cell.setCellValue(batch.getBatchNo());
            cell = cell(row, 'B');
            cell.setCellValue(grade.getName());
            cell = cell(row, 'C');
            cell.setCellValue(getPackageBoxTypeString(packageBoxType));
            cell = cell(row, 'D');
            cell.setCellValue(collectValue.getSilkCount());
            cell = cell(row, 'E');
            cell.setCellValue(collectValue.getSilkWeight().doubleValue());
        }
    }

    private static String getPackageBoxTypeString(PackageBoxType packageBoxType) {
        switch (packageBoxType) {
            case AUTO: {
                return "自动打包";
            }
            case MANUAL: {
                return "人工打包";
            }
            case MANUAL_APPEND: {
                return "补充唛头";
            }
            case SMALL: {
                return "小包装";
            }
            case BIG_SILK_CAR: {
                return "大丝车";
            }
        }
        return "NULL";
    }

    private static void collectMap(Map<CollectKey, CollectValue> map, PackageBox packageBox) {
        final CollectKey collectKey = new CollectKey(packageBox.getBatch(), packageBox.getGrade(), packageBox.getType());
        map.compute(collectKey, (k, v) -> {
            if (v == null) {
                v = new CollectValue();
            }
            v.silkCount += packageBox.getSilkCount();
            final BigDecimal silkWeight = new BigDecimal("" + packageBox.getNetWeight());
            v.silkWeight = v.silkWeight.add(silkWeight);
            return v;
        });
    }

    @SneakyThrows
    private static Collection<PackageBox> packageBoxes(Workshop workshop, LocalDate ld) {
        final String urlTpl = "http://10.2.0.215:9998/api/packageBoxes?pageSize=50000&workshopId=${workshopId}&startDate=${date}&endDate=${date}";
        final Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url(J.strTpl(urlTpl, ImmutableMap.of("workshopId", workshop.getId(), "date", "" + ld)))
                .build();
        @Cleanup final Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            final JsonNode jsonNode = MAPPER.readTree(response.body().byteStream());
            final JsonNode packageBoxesNode = jsonNode.get("packageBoxes");
            final Collection<PackageBox> packageBoxes = Lists.newArrayList();
            packageBoxesNode.forEach(it -> {
                final PackageBox packageBox = MAPPER.convertValue(it, PackageBox.class);
                packageBoxes.add(packageBox);
            });
            return packageBoxes;
        }
        throw new RuntimeException();
    }

    @Data
    private static class CollectKey {
        private final Batch batch;
        private final Grade grade;
        private final PackageBoxType packageBoxType;
    }

    @Data
    private static class CollectValue {
        private int silkCount;
        private BigDecimal silkWeight = BigDecimal.ZERO;
    }

}
