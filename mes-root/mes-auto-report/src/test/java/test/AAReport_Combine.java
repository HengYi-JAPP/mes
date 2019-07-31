package test;

import com.hengyi.japp.mes.auto.report.application.dto.statistic.PoiUtil;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.StatisticReportCombine;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;

/**
 * @author jzb 2019-05-31
 */
@Slf4j
public class AAReport_Combine {

    @SneakyThrows
    public static void main(String[] args) {
        final String[] extensions = {"xlsx"};
//        final Collection<File> files = FileUtils.listFiles(FileUtils.getFile("/home/jzb/C车间7月"), extensions, false);
        final Collection<File> files = FileUtils.listFiles(FileUtils.getFile("/home/jzb/F.7"), extensions, false);
        final StatisticReportCombine report = new StatisticReportCombine(files);
        @Cleanup final Workbook wb = new XSSFWorkbook();
        PoiUtil.fillSheet1(wb.createSheet(), report);
        @Cleanup final FileOutputStream os = new FileOutputStream("/home/jzb/test.xlsx");
        wb.write(os);
    }

}
