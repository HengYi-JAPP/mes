package com.hengyi.japp.mes.auto.report.application.dto.statistic;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.exception.DailyReportNotExistException;
import lombok.*;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;

import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;

/**
 * 统计报表 单日
 *
 * @author jzb 2019-05-29
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StatisticReportDay extends AbstractStatisticReport {
    private LocalDate ld;

    @SneakyThrows
    public static StatisticReportDay from(File file) {
        if (file.exists()) {
            return YAML_MAPPER.readValue(file, StatisticReportDay.class);
        }
        throw new DailyReportNotExistException(file);
    }

    @SneakyThrows
    public StatisticReportDay saveYaml(File file) {
        FileUtils.forceMkdirParent(file);
        YAML_MAPPER.writeValue(file, this);
        return this;
    }

    @SneakyThrows
    public StatisticReportDay testXlsx() {
        @Cleanup final Workbook wb = new XSSFWorkbook();
        PoiUtil.fillSheet1(wb.createSheet("产量"), this);
        if (J.nonEmpty(unDiffPackageBoxes)) {
            PoiUtil.fillSheet2(wb.createSheet("无法分配"), this);
        }
        @Cleanup final FileOutputStream os = new FileOutputStream("/home/jzb/" + workshop.getName() + "." + ld + ".xlsx");
        wb.write(os);
        return this;
    }

}