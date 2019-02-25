package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.report.PoiUtil;
import com.hengyi.japp.mes.auto.repository.PackageBoxRepository;
import com.hengyi.japp.mes.auto.repository.WorkshopRepository;
import io.reactivex.Single;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

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
            PoiUtil.fillData(wb, sheet, report.getItems());
            @Cleanup final ByteArrayOutputStream os = new ByteArrayOutputStream();
            wb.write(os);
            return os.toByteArray();
        });
    }

}
