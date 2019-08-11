package com.hengyi.japp.mes.auto.report.application.dto.statistic;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.BatchDTO;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.GradeDTO;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.LineDTO;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.PackageBoxDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.ixtf.japp.poi.Jpoi.cell;
import static java.util.stream.Collectors.joining;
import static org.apache.poi.ss.usermodel.CellType.FORMULA;
import static org.apache.poi.ss.util.CellUtil.getCell;
import static org.apache.poi.ss.util.CellUtil.getRow;

/**
 * @author jzb 2019-01-21
 */
public class PoiUtil {
    /**
     * 无法分配
     */
    public static void fillSheet2(Sheet sheet, AbstractStatisticReport report) {
        // 粗体行
        final Collection<Row> boldRows = Sets.newHashSet();
        final String[] heads = {"唛头", "批号", "等级", "筒管数", "净重"};
        Row row = addHeads(sheet, heads);
        boldRows.add(row);

        int rowIndex = 0;
        Cell cell = null;
        for (PackageBoxDTO unDiffPackageBox : report.getUnDiffPackageBoxes()) {
            row = getRow(++rowIndex, sheet);
            cell = cell(row, 'A');
            cell.setCellValue(unDiffPackageBox.getCode());
            cell = cell(row, 'B');
            cell.setCellValue(unDiffPackageBox.getBatch().getBatchNo());
            cell = cell(row, 'C');
            cell.setCellValue(unDiffPackageBox.getGrade().getName());
            cell = cell(row, 'D');
            cell.setCellValue(unDiffPackageBox.getSilkCount());
            cell = cell(row, 'E');
            cell.setCellValue(unDiffPackageBox.getNetWeight().doubleValue());
        }
        cssSheet2(sheet, boldRows, rowIndex);
    }

    public static void fillSheet1(Sheet sheet, AbstractStatisticReport report) {
        final Collection<XlsxItem> xlsxItems = XlsxItem.collect(report.getItems());
        // 机台合计行
        final Collection<Row> lineSumRows = Sets.newHashSet();
        // 粗体行
        final Collection<Row> boldRows = Sets.newHashSet();
        final String[] heads = {"机台", "品名", "规格", "批号", "AA", "A", "B", "C", "合计", "筒管数", "优等率", "壹等率", "", "AA筒管数", "A筒管数", "B筒管数", "C筒管数"};
        Row row = addHeads(sheet, heads);
        boldRows.add(row);
        int rowIndex = 1;

        Cell cell = null;
        for (XlsxItem item : xlsxItems) {
            row = getRow(rowIndex, sheet);
            // 线别统计开始行
            final int lineStartRowIndex = rowIndex;
            final LineDTO line = item.getLine();
            for (var batchData : item.getBatchData()) {
                final BatchDTO batch = batchData.getBatch();
                final AbstractStatisticReport.ProductDTO product = batch.getProduct();
                final String[] strings = {line.getName(), product.getName(), batchData.getSpec(), batch.getBatchNo()};
                for (int i = 0, l = strings.length; i < l; i++) {
                    cell = getCell(row, i);
                    cell.setCellValue(strings[i]);
                }
                for (var gradeData : batchData.getGradeData()) {
                    final GradeDTO grade = gradeData.getGrade();
                    final int silkCount = gradeData.getSilkCount();
                    final BigDecimal silkWeight = gradeData.getSilkWeight();
                    switch (grade.getName()) {
                        case "AA": {
                            cell = cell(row, 'E');
                            break;
                        }
                        case "A": {
                            cell = cell(row, 'F');
                            break;
                        }
                        case "B": {
                            cell = cell(row, 'G');
                            break;
                        }
                        case "C": {
                            cell = cell(row, 'H');
                            break;
                        }
                    }
                    final double d = silkWeight.doubleValue();
                    if (d > 0) {
                        cell.setCellValue(d);
                    }
                    getCell(row, cell.getColumnIndex() + 9).setCellValue(silkCount);
                }
                row = getRow(++rowIndex, sheet);
            }
            final int lineEndRowIndex = rowIndex - 1;
            cell = cell(row, 'A');
            cell.setCellValue(line.getName());
            cell = cell(row, 'C');
            cell.setCellValue("机台小计");
            lineSumRows.add(row);

            final Row formulaRow = row;
            Stream.of('E', 'F', 'G', 'H', 'I', 'J', 'N', 'O', 'P', 'Q').forEach(it -> {
                final Cell formulaCell = cell(formulaRow, it);
                formulaCell.setCellFormula("SUM(" + it + (lineStartRowIndex + 1) + ":" + it + (lineEndRowIndex + 1) + ")");
            });
            row = getRow(++rowIndex, sheet);
        }
        boldRows.addAll(lineSumRows);
        boldRows.add(row);
        cell = cell(row, 'C');
        cell.setCellValue("合计");
        final Row totalFormulaRow = row;
        Stream.of('E', 'F', 'G', 'H', 'I', 'J', 'N', 'O', 'P', 'Q').forEach(it -> {
            final Cell formulaCell = cell(totalFormulaRow, it);
            final String sumFormula = lineSumRows.stream().map(lineSumRow -> {
                final int formulaRowIndex = lineSumRow.getRowNum() + 1;
                return "" + it + formulaRowIndex;
            }).collect(joining("+"));
            formulaCell.setCellFormula(sumFormula);
        });
        // 全局公式
        IntStream.rangeClosed(1, rowIndex).forEach(i -> {
            final Row formulaRow = getRow(i, sheet);
            final int formulaRowIndex = i + 1;

            Cell formulaCell = cell(formulaRow, 'I');
            formulaCell.setCellFormula("SUM(E" + (formulaRow.getRowNum() + 1) + ":H" + (formulaRow.getRowNum() + 1) + ")");

            formulaCell = cell(formulaRow, 'J');
            formulaCell.setCellFormula("SUM(N" + (formulaRow.getRowNum() + 1) + ":Q" + (formulaRow.getRowNum() + 1) + ")");

            formulaCell = cell(formulaRow, 'K');
            formulaCell.setCellFormula("E" + formulaRowIndex + "/I" + formulaRowIndex + "*100");
            formulaCell = cell(formulaRow, 'L');
            formulaCell.setCellFormula("(E" + formulaRowIndex + "+F" + formulaRowIndex + ")/I" + formulaRowIndex + "*100");
        });
        sheet.getWorkbook().getCreationHelper().createFormulaEvaluator().evaluateAll();

        // 美化
        cssSheet1(sheet, boldRows, rowIndex);
    }

    private static void cssSheet1(Sheet sheet, Collection<Row> boldRows, int rowCount) {
        sheet.createFreezePane(0, 1, 0, 1);
        IntStream.rangeClosed(0, rowCount).forEach(i -> {
            final Row row = getRow(i, sheet);
            final Font font = getFont(sheet.getWorkbook());
            font.setBold(boldRows.contains(row));
            final CellStyle cellStyle = getCellStyle(sheet.getWorkbook());
            cellStyle.setFont(font);
            IntStream.rangeClosed('A', 'Q').filter(it -> it != 'M')
                    .mapToObj(it -> cell(row, (char) it))
                    .peek(it -> it.setCellStyle(cellStyle))
                    .filter(it -> FORMULA == it.getCellType())
                    .forEach(it -> {
                        double d = it.getNumericCellValue();
                        if (d > 0) {
//                            final DataFormat dataFormat = sheet.getWorkbook().createDataFormat();
//                            cellStyle.setDataFormat(dataFormat.getFormat("0.000"));
                        } else {
                            it.setCellValue("");
//                            it.setBlank();
                        }
                    });
        });
        Stream.of('C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'N', 'O', 'P', 'Q').mapToInt(it -> it - 'A').forEach(sheet::autoSizeColumn);
        sheet.addMergedRegion(new CellRangeAddress(0, rowCount, 'M' - 'A', 'M' - 'A'));
        sheet.addMergedRegion(new CellRangeAddress(rowCount, rowCount, 'A' - 'A', 'B' - 'A'));
    }

    private static void cssSheet2(Sheet sheet, Collection<Row> boldRows, int rowCount) {
        sheet.createFreezePane(0, 1, 0, 1);
        IntStream.rangeClosed(0, rowCount).forEach(i -> {
            final Row row = getRow(i, sheet);
            final Font font = getFont(sheet.getWorkbook());
            font.setBold(boldRows.contains(row));
            final CellStyle cellStyle = getCellStyle(sheet.getWorkbook());
            cellStyle.setFont(font);
            IntStream.rangeClosed('A', 'E')
                    .mapToObj(it -> cell(row, (char) it))
                    .peek(it -> it.setCellStyle(cellStyle))
                    .filter(it -> FORMULA == it.getCellType())
                    .forEach(it -> {
                        double d = it.getNumericCellValue();
                        if (d == 0) {
                            it.setCellValue("");
//                            it.setBlank();
                        }
                    });
        });
        Stream.of('A', 'B').mapToInt(it -> it - 'A').forEach(sheet::autoSizeColumn);
    }

    private static Font getFont(Workbook wb) {
        final Font font = wb.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 14);
        return font;
    }

    private static CellStyle getCellStyle(Workbook wb) {
        final CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        return cellStyle;
    }


    public static Row addHeads(Sheet sheet, String[] heads) {
        final Row row = getRow(0, sheet);
        for (int i = 0, l = heads.length; i < l; i++) {
            final String head = heads[i];
            if (J.isBlank(head)) {
                continue;
            }
            Cell cell = getCell(row, i);
            cell.setCellValue(head);
        }
        row.setHeight((short) 500);
        return row;
    }

}