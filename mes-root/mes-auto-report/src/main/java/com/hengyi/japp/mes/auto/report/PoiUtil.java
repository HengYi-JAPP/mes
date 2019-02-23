package com.hengyi.japp.mes.auto.report;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.application.report.StatisticsReport;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.Product;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellUtil;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author jzb 2019-01-21
 */
public class PoiUtil {
    public static void fillData(Workbook wb, Sheet sheet, Collection<StatisticsReport.Item> items) {
        final Font defaultFont = wb.createFont();
        defaultFont.setFontName("宋体");
        defaultFont.setFontHeightInPoints((short) 14);
        defaultFont.setBold(false);
        final Function<Font, CellStyle> cellStyleFun = font -> {
            final CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            return cellStyle;
        };
        final CellStyle defaultCellStyle = cellStyleFun.apply(defaultFont);
        final BiFunction<Row, Integer, Cell> cellFun = (row, col) -> {
            final Cell cell = CellUtil.getCell(row, col);
            cell.setCellStyle(defaultCellStyle);
            return cell;
        };
        // 小计 合计 字体
        final Font boldFont = wb.createFont();
        boldFont.setFontName("宋体");
        boldFont.setFontHeightInPoints((short) 14);
        boldFont.setBold(true);
        final CellStyle boldCellStyle = cellStyleFun.apply(boldFont);
        final BiFunction<Row, Integer, Cell> boldCellFun = (row, col) -> {
            final Cell cell = CellUtil.getCell(row, col);
            cell.setCellStyle(boldCellStyle);
            return cell;
        };
        // 机台合计行
        final Collection<Row> lineSumRows = Sets.newHashSet();
        final Collection<Row> boldRows = Sets.newHashSet();

        Row row = CellUtil.getRow(0, sheet);
        Cell cell = boldCellFun.apply(row, 0);
        cell.setCellValue("机台");
        cell = boldCellFun.apply(row, 1);
        cell.setCellValue("品名");
        cell = boldCellFun.apply(row, 2);
        cell.setCellValue("规格");
        cell = boldCellFun.apply(row, 3);
        cell.setCellValue("批号");
        cell = boldCellFun.apply(row, 4);
        cell.setCellValue("AA");
        cell = boldCellFun.apply(row, 5);
        cell.setCellValue("A");
        cell = boldCellFun.apply(row, 6);
        cell.setCellValue("B");
        cell = boldCellFun.apply(row, 7);
        cell.setCellValue("C");
        cell = boldCellFun.apply(row, 8);
        cell.setCellValue("合计");
        cell = boldCellFun.apply(row, 9);
        cell.setCellValue("筒管数");
        cell = boldCellFun.apply(row, 10);
        cell.setCellValue("优等率");
        cell = boldCellFun.apply(row, 11);
        cell.setCellValue("壹等率");

        final List<StatisticsReport.XlsxItem> xlsxItems = items.parallelStream()
                .collect(Collectors.groupingBy(StatisticsReport.Item::getLine))
                .entrySet().parallelStream()
                .map(entry -> {
                    final Line line = entry.getKey();
                    final HashMultimap<Batch, Triple<Grade, Integer, BigDecimal>> batchMultimap = HashMultimap.create();
                    entry.getValue().forEach(item -> {
                        final Batch batch = item.getBatch();
                        final Grade grade = item.getGrade();
                        final int silkCount = item.getSilkCount();
                        final BigDecimal silkWeight = item.getSilkWeight();
                        batchMultimap.put(batch, Triple.of(grade, silkCount, silkWeight));
                    });
                    return new StatisticsReport.XlsxItem(line, batchMultimap);
                }).sorted()
                .collect(Collectors.toList());

        int rowIndex = 1;
        for (StatisticsReport.XlsxItem item : xlsxItems) {
            row = CellUtil.getRow(rowIndex, sheet);
            final int lineStartRowIndex = rowIndex;
            final Line line = item.getLine();
            final Multimap<Batch, Triple<Grade, Integer, BigDecimal>> batchMultimap = item.getBatchMultimap();
            for (Batch batch : Sets.newTreeSet(batchMultimap.keySet())) {
                final Product product = batch.getProduct();
                cell = cellFun.apply(row, 0);
                cell.setCellValue(line.getName());
                cell = cellFun.apply(row, 1);
                cell.setCellValue(product.getName());
                cell = cellFun.apply(row, 2);
                cell.setCellValue(batch.getSpec());
                cell = cellFun.apply(row, 3);
                cell.setCellValue(batch.getBatchNo());
                int sumSilkCount = 0;
                for (Triple<Grade, Integer, BigDecimal> triple : batchMultimap.get(batch)) {
                    final Grade grade = triple.getLeft();
                    final Integer silkCount = triple.getMiddle();
                    sumSilkCount += silkCount;
                    final BigDecimal silkWeight = triple.getRight();
                    switch (grade.getName()) {
                        case "AA": {
                            cell = cellFun.apply(row, 4);
                            break;
                        }
                        case "A": {
                            cell = cellFun.apply(row, 5);
                            break;
                        }
                        case "B": {
                            cell = cellFun.apply(row, 6);
                            break;
                        }
                        case "C": {
                            cell = cellFun.apply(row, 7);
                            break;
                        }
                    }
                    cell.setCellValue(silkWeight.doubleValue());
                }
                cell = cellFun.apply(row, 9);
                cell.setCellValue(sumSilkCount);
                row = CellUtil.getRow(++rowIndex, sheet);
            }
            final int lineEndRowIndex = rowIndex - 1;
            cell = boldCellFun.apply(row, 0);
            cell.setCellValue(line.getName());
            cell = boldCellFun.apply(row, 2);
            cell.setCellValue("机台小计");
            lineSumRows.add(row);

            final Row formulaRow = row;
            Stream.of("E", "F", "G", "H", "I", "J").forEach(it -> {
                // E = 69
                final int colIndex = it.charAt(0) - 65;
                final Cell formulaCell = boldCellFun.apply(formulaRow, colIndex);
                formulaCell.setCellFormula("SUM(" + it + (lineStartRowIndex + 1) + ":" + it + (lineEndRowIndex + 1) + ")");
            });
            row = CellUtil.getRow(++rowIndex, sheet);
        }
        boldRows.add(row);
        cell = boldCellFun.apply(row, 2);
        cell.setCellValue("合计");
        final Row totalFormulaRow = row;
        Stream.of("E", "F", "G", "H", "I", "J").forEach(it -> {
            // E = 69
            final int colIndex = it.charAt(0) - 65;
            final Cell formulaCell = boldCellFun.apply(totalFormulaRow, colIndex);
            final String sumFormula = lineSumRows.stream().map(lineSumRow -> {
                final int formulaRowIndex = lineSumRow.getRowNum() + 1;
                return it + formulaRowIndex;
            }).collect(Collectors.joining("+"));
            formulaCell.setCellFormula(sumFormula);
        });

        IntStream.rangeClosed(1, rowIndex).forEach(i -> {
            final Row formulaRow = CellUtil.getRow(i, sheet);
            final BiFunction<Row, Integer, Cell> cFun = lineSumRows.contains(formulaRow) ? boldCellFun : cellFun;
            final int formulaRowIndex = i + 1;
            Cell formulaCell = cFun.apply(formulaRow, 8);
            final String sumFormula = Stream.of("E", "F", "G", "H")
                    .map(it -> it + formulaRowIndex)
                    .collect(Collectors.joining("+"));
            formulaCell.setCellFormula(sumFormula);
            formulaCell = cFun.apply(formulaRow, 10);
            formulaCell.setCellFormula("E" + formulaRowIndex + "/I" + formulaRowIndex + "*100");
            formulaCell = cFun.apply(formulaRow, 11);
            formulaCell.setCellFormula("(E" + formulaRowIndex + "+F" + formulaRowIndex + ")/I" + formulaRowIndex + "*100");
        });
        wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
//        IntStream.rangeClosed(0,11).forEach(sheet::autoSizeColumn);
    }
}
