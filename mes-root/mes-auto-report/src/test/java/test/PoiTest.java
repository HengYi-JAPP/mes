package test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.poi.Jpoi;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.*;
import org.bson.Document;

import java.io.File;
import java.io.FileOutputStream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;

/**
 * @author jzb 2019-10-12
 */
public class PoiTest {

    private static Workshop findWorkshop(File jsonFile) {
        final String workshopId = FilenameUtils.getBaseName(jsonFile.getName());
        final Document document = QueryService.findFromCache(Workshop.class, workshopId).get();
        final Workshop workshop = new Workshop();
        workshop.setId(document.getString(ID_COL));
        workshop.setName(document.getString("name"));
        return workshop;
    }

    @SneakyThrows
    public static void PackageBoxReport(File file) {
        @Cleanup final Workbook wb = WorkbookFactory.create(true);
        for (File jsonFile : FileUtils.listFiles(file.getParentFile(), new String[]{"json"}, false)) {
            final Workshop workshop = findWorkshop(jsonFile);
            final Sheet sheet = wb.createSheet(workshop.getName());
            final JsonNode jsonNode = MAPPER.readTree(jsonFile);
            int rowIdx = 0;
            Row row = sheet.createRow(rowIdx);
            Cell cell = Jpoi.cell(row, 'A');
            cell.setCellValue("人员");
            cell = Jpoi.cell(row, 'B');
            cell.setCellValue("包数");
            cell = Jpoi.cell(row, 'C');
            cell.setCellValue("颗数");
            cell = Jpoi.cell(row, 'D');
            cell.setCellValue("净重");
            for (JsonNode node : jsonNode) {
                row = sheet.createRow(++rowIdx);
                final JsonNode operator = node.get("operator");
                cell = Jpoi.cell(row, 'A');
                cell.setCellValue(operator.get("name").asText());
                cell = Jpoi.cell(row, 'B');
                cell.setCellValue(node.get("packageBoxCount").asLong());
                cell = Jpoi.cell(row, 'C');
                cell.setCellValue(node.get("silkCountSum").asLong());
                cell = Jpoi.cell(row, 'D');
                cell.setCellValue(node.get("netWeightSum").asDouble());
            }
        }
        @Cleanup final FileOutputStream os = new FileOutputStream(file);
        wb.write(os);
    }

    @SneakyThrows
    public static void ExceptionRecordReport(File file) {
        @Cleanup final Workbook wb = WorkbookFactory.create(true);
        for (File jsonFile : FileUtils.listFiles(file.getParentFile(), new String[]{"json"}, false)) {
            final Workshop workshop = findWorkshop(jsonFile);
            final Sheet sheet = wb.createSheet(workshop.getName());
            final JsonNode jsonNode = MAPPER.readTree(jsonFile);
            int rowIdx = 0;
            Row row = sheet.createRow(rowIdx);
            Cell cell = Jpoi.cell(row, 'A');
            cell.setCellValue("人员");
            cell = Jpoi.cell(row, 'B');
            cell.setCellValue("颗数");
            for (JsonNode node : jsonNode) {
                row = sheet.createRow(++rowIdx);
                final JsonNode operator = node.get("operator");
                cell = Jpoi.cell(row, 'A');
                cell.setCellValue(operator.get("name").asText());
                cell = Jpoi.cell(row, 'B');
                cell.setCellValue(node.get("silkCount").asLong());
            }
        }
        @Cleanup final FileOutputStream os = new FileOutputStream(file);
        wb.write(os);
    }

    @SneakyThrows
    public static void InspectionReport(File file) {
        @Cleanup final Workbook wb = WorkbookFactory.create(true);
        for (File jsonFile : FileUtils.listFiles(file.getParentFile(), new String[]{"json"}, false)) {
            final Workshop workshop = findWorkshop(jsonFile);
            final Sheet sheet = wb.createSheet(workshop.getName());
            final JsonNode jsonNode = MAPPER.readTree(jsonFile);
            int rowIdx = 0;
            Row row = sheet.createRow(rowIdx);
            Cell cell = Jpoi.cell(row, 'A');
            cell.setCellValue("人员");
            cell = Jpoi.cell(row, 'B');
            cell.setCellValue("车数");
            cell = Jpoi.cell(row, 'C');
            cell.setCellValue("颗数");
//            cell = Jpoi.cell(row, 'D');
//            cell.setCellValue("净重");
            for (JsonNode node : jsonNode) {
                row = sheet.createRow(++rowIdx);
                final JsonNode operator = node.get("operator");
                cell = Jpoi.cell(row, 'A');
                cell.setCellValue(operator.get("name").asText());
                cell = Jpoi.cell(row, 'B');
                cell.setCellValue(node.get("silkCarRecordCount").asLong());
                cell = Jpoi.cell(row, 'C');
                cell.setCellValue(node.get("silkCount").asLong());
//                cell = Jpoi.cell(row, 'D');
//                cell.setCellValue(node.get("netWeightSum").asDouble());
            }
        }
        @Cleanup final FileOutputStream os = new FileOutputStream(file);
        wb.write(os);
    }

    @SneakyThrows
    public static void DoffingReport(File file) {
        @Cleanup final Workbook wb = WorkbookFactory.create(true);
        for (File jsonFile : FileUtils.listFiles(file.getParentFile(), new String[]{"json"}, false)) {
            final Workshop workshop = findWorkshop(jsonFile);
            final Sheet sheet = wb.createSheet(workshop.getName());
            final JsonNode jsonNode = MAPPER.readTree(jsonFile);
            int rowIdx = 0;
            Row row = sheet.createRow(rowIdx);
            Cell cell = Jpoi.cell(row, 'A');
            cell.setCellValue("人员");
            cell = Jpoi.cell(row, 'B');
            cell.setCellValue("车数");
            cell = Jpoi.cell(row, 'C');
            cell.setCellValue("颗数");
//            cell = Jpoi.cell(row, 'D');
//            cell.setCellValue("净重");
            for (JsonNode node : jsonNode) {
                row = sheet.createRow(++rowIdx);
                final JsonNode operator = node.get("operator");
                cell = Jpoi.cell(row, 'A');
                cell.setCellValue(operator.get("name").asText());
                cell = Jpoi.cell(row, 'B');
                cell.setCellValue(node.get("silkCarRecordCount").asLong());
                cell = Jpoi.cell(row, 'C');
                cell.setCellValue(node.get("silkCount").asLong());
//                cell = Jpoi.cell(row, 'D');
//                cell.setCellValue(node.get("netWeightSum").asDouble());
            }
        }
        @Cleanup final FileOutputStream os = new FileOutputStream(file);
        wb.write(os);
    }
}
