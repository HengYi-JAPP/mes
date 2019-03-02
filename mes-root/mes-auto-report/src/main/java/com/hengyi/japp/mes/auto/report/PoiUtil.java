package com.hengyi.japp.mes.auto.report;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.application.report.StatisticsReport;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.Product;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static org.apache.poi.ss.usermodel.CellType.BLANK;
import static org.apache.poi.ss.usermodel.CellType.FORMULA;
import static org.apache.poi.ss.util.CellUtil.getCell;
import static org.apache.poi.ss.util.CellUtil.getRow;

/**
 * @author jzb 2019-01-21
 */
public class PoiUtil {

    public static void fillData(Workbook wb, Sheet sheet, Collection<StatisticsReport.Item> items) {
        final List<StatisticsReport.XlsxItem> xlsxItems = aaCollect(items).parallelStream()
                .collect(groupingBy(StatisticsReport.Item::getLine))
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
                }).collect(toList());
        Collections.sort(xlsxItems);

        // 机台合计行
        final Collection<Row> lineSumRows = Sets.newHashSet();
        // 粗体行
        final Collection<Row> boldRows = Sets.newHashSet();

        Row row = getRow(0, sheet);
        Cell cell = null;

        boldRows.add(row);
        final String[] heads = {"机台", "品名", "规格", "批号", "AA", "A", "B", "C", "合计", "筒管数", "优等率", "壹等率", "", "AA筒管数", "A筒管数", "B筒管数", "C筒管数"};
        for (int i = 0, l = heads.length; i < l; i++) {
            final String head = heads[i];
            if (J.isBlank(head)) {
                continue;
            }
            cell = getCell(row, i);
            cell.setCellValue(head);
        }

        int rowIndex = 1;
        for (StatisticsReport.XlsxItem item : xlsxItems) {
            row = getRow(rowIndex, sheet);
            // 线别统计开始行
            final int lineStartRowIndex = rowIndex;
            final Line line = item.getLine();
            final Multimap<Batch, Triple<Grade, Integer, BigDecimal>> batchMultimap = item.getBatchMultimap();
            for (Batch batch : Sets.newTreeSet(batchMultimap.keySet())) {
                final Product product = batch.getProduct();
                final String[] strings = {line.getName(), product.getName(), batch.getSpec(), batch.getBatchNo()};
                for (int i = 0, l = strings.length; i < l; i++) {
                    cell = getCell(row, i);
                    cell.setCellValue(strings[i]);
                }
                final int aaSilkWeightCol = 'E' - 'A';
                for (var triple : batchMultimap.get(batch)) {
                    final Grade grade = triple.getLeft();
                    final Integer silkCount = triple.getMiddle();
                    final BigDecimal silkWeight = triple.getRight();
                    switch (grade.getName()) {
                        case "AA": {
                            cell = getCell(row, aaSilkWeightCol);
                            break;
                        }
                        case "A": {
                            cell = getCell(row, aaSilkWeightCol + 1);
                            break;
                        }
                        case "B": {
                            cell = getCell(row, aaSilkWeightCol + 2);
                            break;
                        }
                        case "C": {
                            cell = getCell(row, aaSilkWeightCol + 3);
                            break;
                        }
                    }
                    cell.setCellValue(silkWeight.doubleValue());
                    getCell(row, cell.getColumnIndex() + 9).setCellValue(silkCount);
                }
                row = getRow(++rowIndex, sheet);
            }
            final int lineEndRowIndex = rowIndex - 1;
            cell = getCell(row, 0);
            cell.setCellValue(line.getName());
            cell = getCell(row, 2);
            cell.setCellValue("机台小计");
            lineSumRows.add(row);

            final Row formulaRow = row;
            Stream.of('E', 'F', 'G', 'H', 'I', 'J', 'N', 'O', 'P', 'Q').forEach(it -> {
                final Cell formulaCell = getCell(formulaRow, it - 'A');
                formulaCell.setCellFormula("SUM(" + it + (lineStartRowIndex + 1) + ":" + it + (lineEndRowIndex + 1) + ")");
            });
            row = getRow(++rowIndex, sheet);
        }
        boldRows.addAll(lineSumRows);
        boldRows.add(row);
        cell = getCell(row, 'C' - 'A');
        cell.setCellValue("合计");
        final Row totalFormulaRow = row;
        Stream.of('E', 'F', 'G', 'H', 'I', 'J', 'N', 'O', 'P', 'Q').forEach(it -> {
            final Cell formulaCell = getCell(totalFormulaRow, it - 'A');
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

            Cell formulaCell = getCell(formulaRow, 'I' - 'A');
            String sumFormula = Stream.of("E", "F", "G", "H")
                    .map(it -> it + formulaRowIndex)
                    .collect(joining("+"));
            formulaCell.setCellFormula(sumFormula);

            formulaCell = getCell(formulaRow, 'J' - 'A');
            sumFormula = Stream.of("N", "O", "P", "Q")
                    .map(it -> it + formulaRowIndex)
                    .collect(joining("+"));
            formulaCell.setCellFormula(sumFormula);

            formulaCell = getCell(formulaRow, 'K' - 'A');
            formulaCell.setCellFormula("E" + formulaRowIndex + "/I" + formulaRowIndex + "*100");
            formulaCell = getCell(formulaRow, 'L' - 'A');
            formulaCell.setCellFormula("(E" + formulaRowIndex + "+F" + formulaRowIndex + ")/I" + formulaRowIndex + "*100");
        });
        wb.getCreationHelper().createFormulaEvaluator().evaluateAll();

        // 美化
        IntStream.rangeClosed(0, rowIndex).forEach(i -> {
            final Row cssRow = getRow(i, sheet);
            if (i == 0) {
                cssRow.setHeight((short) 500);
            }
            final Font font = wb.createFont();
            font.setFontName("宋体");
            font.setFontHeightInPoints((short) 14);
            font.setBold(boldRows.contains(cssRow));
            final CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            IntStream.rangeClosed('A', 'Q').filter(it -> it != 'M')
                    .mapToObj(it -> getCell(cssRow, it - 'A'))
                    .forEach(it -> {
                        it.setCellStyle(cellStyle);
                        if (FORMULA == it.getCellType()) {
                            double d = it.getNumericCellValue();
                            if (d == 0) {
                                it.setCellType(BLANK);
                            }
                        }
                    });
        });
        Stream.of('C', 'D', 'I', 'N', 'O', 'P', 'Q').mapToInt(it -> it - 'A').forEach(sheet::autoSizeColumn);
        sheet.addMergedRegion(new CellRangeAddress(0, rowIndex, 'M' - 'A', 'M' - 'A'));
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 'A' - 'A', 'B' - 'A'));
    }

    // 报表体现需要把 AAA 加到 AA 中
    private static Collection<StatisticsReport.Item> aaCollect(Collection<StatisticsReport.Item> originalItems) {
        final var aaCollect = originalItems.parallelStream().filter(it -> {
            final Grade grade = it.getGrade();
            return grade.getSortBy() >= 100;
        }).collect(groupingBy(it -> Pair.of(it.getLine(), it.getBatch())));
        final Grade aaGrade = aaCollect.values().parallelStream()
                .flatMap(Collection::parallelStream)
                .map(StatisticsReport.Item::getGrade)
                .filter(it -> "AA".equals(it.getName()))
                .findAny()
                .orElse(null);
        if (aaGrade == null) {
            return originalItems;
        }
        final var aaStream = aaCollect.entrySet().parallelStream().map(entry -> {
            final Pair<Line, Batch> pair = entry.getKey();
            final Line line = pair.getLeft();
            final Batch batch = pair.getRight();
            final StatisticsReport.Item item = new StatisticsReport.Item(line, batch, aaGrade);
            final int silkCount = entry.getValue().parallelStream().mapToInt(StatisticsReport.Item::getSilkCount).sum();
            item.setSilkCount(silkCount);
            final BigDecimal silkWeight = entry.getValue().parallelStream().map(StatisticsReport.Item::getSilkWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setSilkWeight(silkWeight);
            return item;
        });
        final var abcStream = originalItems.parallelStream().filter(it -> {
            final Grade grade = it.getGrade();
            return grade.getSortBy() < 100;
        });
        return Stream.concat(abcStream, aaStream).parallel().collect(toList());
    }

}
