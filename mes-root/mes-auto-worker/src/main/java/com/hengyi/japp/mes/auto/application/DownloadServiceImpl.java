package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.poi.Jpoi;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.report.StatisticsReportDay;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.Product;
import com.hengyi.japp.mes.auto.repository.PackageBoxRepository;
import com.hengyi.japp.mes.auto.repository.WorkshopRepository;
import io.reactivex.Single;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jzb 2018-08-08
 */
@Slf4j
@Singleton
public class DownloadServiceImpl implements DownloadService {
    private final ReportService reportService;
    private final PackageBoxRepository packageBoxRepository;
    private final WorkshopRepository workshopRepository;

    @Inject
    private DownloadServiceImpl(ReportService reportService, PackageBoxRepository packageBoxRepository, WorkshopRepository workshopRepository) {
        this.reportService = reportService;
        this.packageBoxRepository = packageBoxRepository;
        this.workshopRepository = workshopRepository;
    }

    @Override
    public Single<byte[]> test() {
        final LocalDate ld = LocalDate.of(2019, 1, 15);
        return statisticsReport(null, ld, ld).flatMap(report -> {
            System.out.println(report);
            return Single.fromCallable(() -> {
//                @Cleanup final Workbook wb = new XSSFWorkbook();
//                @Cleanup final ByteArrayOutputStream os = new ByteArrayOutputStream();
//                wb.write(os);
//                return os.toByteArray();
                return report;
            });
        });
    }

    @Override
    public Single<byte[]> statisticsReport(String workshopId, LocalDate startLd, LocalDate endLd) {
        return reportService.statisticsReport(workshopId, startLd, endLd).map(report -> {
            @Cleanup final Workbook wb = new XSSFWorkbook();
            final Sheet sheet = wb.createSheet();

            Cell cell = Jpoi.cell(sheet, 0, 0);
            cell.setCellValue("线别");
            cell = Jpoi.cell(sheet, 0, 1);
            cell.setCellValue("品名");
            cell = Jpoi.cell(sheet, 0, 2);
            cell.setCellValue("规格");
            cell = Jpoi.cell(sheet, 0, 3);
            cell.setCellValue("批号");
            cell = Jpoi.cell(sheet, 0, 4);
            cell.setCellValue("AA");
            cell = Jpoi.cell(sheet, 0, 5);
            cell.setCellValue("B");
            cell = Jpoi.cell(sheet, 0, 6);
            cell.setCellValue("C");

            final List<StatisticsReportDay.XlsxItem> xlsxItems = J.emptyIfNull(report.getItems()).stream()
                    .collect(Collectors.groupingBy(it -> Pair.of(it.getLine(), it.getBatch())))
                    .entrySet().stream()
                    .sorted(Comparator.comparing(it -> {
                        final Pair<Line, Batch> pair = it.getKey();
                        final Line line = pair.getLeft();
                        final Batch batch = pair.getRight();
                        return line.getName() + batch.getBatchNo();
                    }))
                    .map(entry -> {
                        final Pair<Line, Batch> pair = entry.getKey();
                        final Line line = pair.getLeft();
                        final Batch batch = pair.getRight();
                        final StatisticsReportDay.XlsxItem xlsxItem = new StatisticsReportDay.XlsxItem(line, batch);
                        J.emptyIfNull(entry.getValue()).forEach(it -> {
                            final Grade grade = it.getGrade();
                            final Pair<Integer, BigDecimal> value = Pair.of(it.getSilkCount(), it.getSilkWeight());
                            xlsxItem.getGradePairMap().put(grade, value);
                        });
                        return xlsxItem;
                    })
                    .collect(Collectors.toList());

            int rowIndex = 1;
            for (StatisticsReportDay.XlsxItem item : J.emptyIfNull(xlsxItems)) {
                final Line line = item.getLine();
                final Batch batch = item.getBatch();
                final Product product = batch.getProduct();
                cell = Jpoi.cell(sheet, rowIndex, 0);
                cell.setCellValue(line.getName());
                cell = Jpoi.cell(sheet, rowIndex, 1);
                cell.setCellValue(product.getName());
                cell = Jpoi.cell(sheet, rowIndex, 2);
                cell.setCellValue(batch.getSpec());
                cell = Jpoi.cell(sheet, rowIndex, 3);
                cell.setCellValue(batch.getBatchNo());
                for (Grade grade : item.getGradePairMap().keySet()) {
                    final Pair<Integer, BigDecimal> pair = item.getGradePairMap().get(grade);
                    if ("AA".equals(grade.getName())) {
                        cell = Jpoi.cell(sheet, rowIndex, 4);
                        cell.setCellValue(pair.getRight().toString());
                    } else if ("A".equals(grade.getName())) {
                        cell = Jpoi.cell(sheet, rowIndex, 5);
                        cell.setCellValue(pair.getRight().toString());
                    } else if ("B".equals(grade.getName())) {
                        cell = Jpoi.cell(sheet, rowIndex, 6);
                        cell.setCellValue(pair.getRight().toString());
                    } else if ("C".equals(grade.getName())) {
                        cell = Jpoi.cell(sheet, rowIndex, 7);
                        cell.setCellValue(pair.getRight().toString());
                    }
                }
                rowIndex++;
            }
            @Cleanup final ByteArrayOutputStream os = new ByteArrayOutputStream();
            wb.write(os);
            return os.toByteArray();
        });
    }

}
