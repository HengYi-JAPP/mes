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
import org.apache.poi.ss.util.CellUtil;
import org.bson.Document;

import java.io.File;
import java.io.FileOutputStream;

import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;

/**
 * @author jzb 2019-10-12
 */
public class PoiTest {
    private static final String[] EXTENSIONS = {"yml"};

    private static Workshop findWorkshop(File jsonFile) {
        final String workshopId = FilenameUtils.getBaseName(jsonFile.getName());
        final Document document = QueryService.findFromCache(Workshop.class, workshopId).get();
        final Workshop workshop = new Workshop();
        workshop.setId(document.getString(ID_COL));
        workshop.setName(document.getString("name"));
        return workshop;
    }

    private static void addHeaders(Row row, String... headers) {
        for (int i = 0; i < headers.length; i++) {
            CellUtil.createCell(row, i, headers[i]);
        }
    }

    @SneakyThrows
    public static void ExceptionRecordReport(File file) {
        @Cleanup final Workbook wb = WorkbookFactory.create(true);
        for (File ymlFile : FileUtils.listFiles(file.getParentFile(), EXTENSIONS, false)) {
            final Workshop workshop = findWorkshop(ymlFile);
            final Sheet sheet = wb.createSheet(workshop.getName());
            final JsonNode jsonNode = YAML_MAPPER.readTree(ymlFile);
            int rowIdx = 0;
            Row row = sheet.createRow(rowIdx);
            addHeaders(row, "人员", "产品", "颗数");
            for (JsonNode node : jsonNode) {
                final JsonNode operator = node.get("operator");
                for (JsonNode products : node.get("products")) {
                    row = sheet.createRow(++rowIdx);
                    final JsonNode product = products.get("product");
                    Cell cell = Jpoi.cell(row, 'A');
                    cell.setCellValue(operator.get("name").asText());
                    cell = Jpoi.cell(row, 'B');
                    cell.setCellValue(product.get("name").asText());
                    cell = Jpoi.cell(row, 'C');
                    cell.setCellValue(products.get("silkCount").asLong());
                }
            }
        }
        @Cleanup final FileOutputStream os = new FileOutputStream(file);
        wb.write(os);
    }

    @SneakyThrows
    public static void PackageBoxReport(File file) {
        @Cleanup final Workbook wb = WorkbookFactory.create(true);
        for (File ymlFile : FileUtils.listFiles(file.getParentFile(), EXTENSIONS, false)) {
            final Workshop workshop = findWorkshop(ymlFile);
            final Sheet sheet = wb.createSheet(workshop.getName());
            final JsonNode jsonNode = YAML_MAPPER.readTree(ymlFile);
            int rowIdx = 0;
            Row row = sheet.createRow(rowIdx);
            addHeaders(row, "人员", "产品", "包数", "颗数", "净重");
            for (JsonNode node : jsonNode) {
                final JsonNode operator = node.get("operator");
                for (JsonNode products : node.get("products")) {
                    row = sheet.createRow(++rowIdx);
                    final JsonNode product = products.get("product");
                    Cell cell = Jpoi.cell(row, 'A');
                    cell.setCellValue(operator.get("name").asText());
                    cell = Jpoi.cell(row, 'B');
                    cell.setCellValue(product.get("name").asText());
                    cell = Jpoi.cell(row, 'C');
                    cell.setCellValue(products.get("packageBoxCount").asLong());
                    cell = Jpoi.cell(row, 'D');
                    cell.setCellValue(products.get("silkCountSum").asLong());
                    cell = Jpoi.cell(row, 'E');
                    cell.setCellValue(products.get("netWeightSum").asDouble());
                }
            }
        }
        @Cleanup final FileOutputStream os = new FileOutputStream(file);
        wb.write(os);
    }

    @SneakyThrows
    public static void InspectionReport(File file) {
        @Cleanup final Workbook wb = WorkbookFactory.create(true);
        for (File jsonFile : FileUtils.listFiles(file.getParentFile(), EXTENSIONS, false)) {
            final Workshop workshop = findWorkshop(jsonFile);
            final Sheet sheet = wb.createSheet(workshop.getName());
            final JsonNode jsonNode = YAML_MAPPER.readTree(jsonFile);
            int rowIdx = 0;
            Row row = sheet.createRow(rowIdx);
            addHeaders(row, "人员", "产品", "车数", "颗数");
            for (JsonNode node : jsonNode) {
                final JsonNode operator = node.get("operator");
                for (JsonNode products : node.get("products")) {
                    row = sheet.createRow(++rowIdx);
                    final JsonNode product = products.get("product");
                    Cell cell = Jpoi.cell(row, 'A');
                    cell.setCellValue(operator.get("name").asText());
                    cell = Jpoi.cell(row, 'B');
                    cell.setCellValue(product.get("name").asText());
                    cell = Jpoi.cell(row, 'C');
                    cell.setCellValue(products.get("silkCarRecordCount").asLong());
                    cell = Jpoi.cell(row, 'D');
                    cell.setCellValue(products.get("silkCount").asLong());
                }
            }
        }
        @Cleanup final FileOutputStream os = new FileOutputStream(file);
        wb.write(os);
    }

    @SneakyThrows
    public static void DoffingReport(File file) {
        @Cleanup final Workbook wb = WorkbookFactory.create(true);
        for (File jsonFile : FileUtils.listFiles(file.getParentFile(), EXTENSIONS, false)) {
            final Workshop workshop = findWorkshop(jsonFile);
            final Sheet sheet = wb.createSheet(workshop.getName());
            final JsonNode jsonNode = YAML_MAPPER.readTree(jsonFile);
            int rowIdx = 0;
            Row row = sheet.createRow(rowIdx);
            addHeaders(row, "人员", "产品", "车数", "颗数");
            for (JsonNode node : jsonNode) {
                final JsonNode operator = node.get("operator");
                for (JsonNode products : node.get("products")) {
                    row = sheet.createRow(++rowIdx);
                    final JsonNode product = products.get("product");
                    Cell cell = Jpoi.cell(row, 'A');
                    cell.setCellValue(operator.get("name").asText());
                    cell = Jpoi.cell(row, 'B');
                    cell.setCellValue(product.get("name").asText());
                    cell = Jpoi.cell(row, 'C');
                    cell.setCellValue(products.get("silkCarRecordCount").asLong());
                    cell = Jpoi.cell(row, 'D');
                    cell.setCellValue(products.get("silkCount").asLong());
                }
            }
        }
        @Cleanup final FileOutputStream os = new FileOutputStream(file);
        wb.write(os);
    }
}
