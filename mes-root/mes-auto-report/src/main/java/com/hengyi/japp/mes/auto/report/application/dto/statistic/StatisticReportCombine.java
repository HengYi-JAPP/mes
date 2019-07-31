package com.hengyi.japp.mes.auto.report.application.dto.statistic;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.poi.Jpoi;
import lombok.Cleanup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * 统计报表 单日
 *
 * @author jzb 2019-05-29
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StatisticReportCombine extends AbstractStatisticReport {

    public StatisticReportCombine(Collection<File> files) {
        this.items = J.emptyIfNull(files).parallelStream()
                .map(StatisticReportCombine::items)
                .flatMap(StatisticReportCombine::collect)
                .collect(toList());
    }

    private static Stream<Item> collect(Collection<Item> items) {
        return items.parallelStream()
                .collect(groupingBy(Function.identity()))
                .entrySet().parallelStream()
                .map(entry -> {
                    final Item item = entry.getKey();
                    final Integer silkCount = entry.getValue().parallelStream().collect(summingInt(Item::getSilkCount));
                    item.setSilkCount(silkCount);
                    final BigDecimal silkWeight = entry.getValue().parallelStream().map(Item::getSilkWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
                    item.setSilkWeight(silkWeight);
                    return item;
                });
    }

    private static Collection<Item> items(File file) {
        try {
            @Cleanup final Workbook wb = WorkbookFactory.create(file);
            final Sheet sheet = wb.getSheetAt(0);
            return IntStream.rangeClosed(sheet.getFirstRowNum(), sheet.getLastRowNum())
                    .mapToObj(sheet::getRow)
                    .filter(row -> Objects.nonNull(row)
                            && J.nonBlank(getString(row, 'A'))
                            && J.nonBlank(getString(row, 'B'))
                            && J.nonBlank(getString(row, 'C'))
                            && J.nonBlank(getString(row, 'D'))
                            && Objects.nonNull(getBigDecimal(row, 'E'))
                            && Objects.nonNull(getBigDecimal(row, 'F'))
                            && Objects.nonNull(getBigDecimal(row, 'G'))
                            && Objects.nonNull(getBigDecimal(row, 'H'))
                            && Objects.nonNull(getBigDecimal(row, 'J'))
                    )
                    .flatMap(row -> {
                        final String lineName = getString(row, 'A');
                        final LineDTO line = new LineDTO();
                        line.setId(lineName);
                        line.setName(lineName);

                        final String productName = getString(row, 'B');
                        final ProductDTO product = new ProductDTO();
                        product.setId(productName);
                        product.setName(productName);
                        final String spec = getString(row, 'C');
                        final String batchNo = getString(row, 'D');
                        final BatchDTO batch = new BatchDTO();
                        batch.setId(batchNo);
                        batch.setBatchNo(batchNo);
                        batch.setSpec(spec);
                        batch.setProduct(product);

                        Collection<Item> items = Lists.newArrayList();
                        final int silkCount = getBigDecimal(row, 'J').intValue();
                        boolean silkCountAdded = false;
                        final BigDecimal aaWeight = getBigDecimal(row, 'E');
                        if (aaWeight.intValue() > 0) {
                            final GradeDTO aaGrade = new GradeDTO();
                            aaGrade.setName("AA");
                            aaGrade.setId("AA");
                            aaGrade.setSortBy(100);
                            final Item item = new Item(false, line, batch, aaGrade);
                            item.setSilkWeight(aaWeight);
                            item.setSilkCount(silkCount);
                            silkCountAdded = true;
                            items.add(item);
                        }

                        final BigDecimal aWeight = getBigDecimal(row, 'F');
                        if (aWeight.intValue() > 0) {
                            final GradeDTO aGrade = new GradeDTO();
                            aGrade.setName("A");
                            aGrade.setId("A");
                            aGrade.setSortBy(90);
                            final Item item = new Item(false, line, batch, aGrade);
                            item.setSilkWeight(aaWeight);
                            if (!silkCountAdded) {
                                item.setSilkCount(silkCount);
                            }
                            silkCountAdded = true;
                            items.add(item);
                        }
                        final BigDecimal bWeight = getBigDecimal(row, 'G');
                        if (bWeight.intValue() > 0) {
                            final GradeDTO bGrade = new GradeDTO();
                            bGrade.setName("B");
                            bGrade.setId("B");
                            bGrade.setSortBy(80);
                            final Item item = new Item(false, line, batch, bGrade);
                            item.setSilkWeight(aaWeight);
                            if (!silkCountAdded) {
                                item.setSilkCount(silkCount);
                            }
                            silkCountAdded = true;
                            items.add(item);
                        }
                        final BigDecimal cWeight = getBigDecimal(row, 'H');
                        if (cWeight.intValue() > 0) {
                            final GradeDTO cGrade = new GradeDTO();
                            cGrade.setName("C");
                            cGrade.setId("C");
                            cGrade.setSortBy(70);
                            final Item item = new Item(false, line, batch, cGrade);
                            item.setSilkWeight(aaWeight);
                            if (!silkCountAdded) {
                                item.setSilkCount(silkCount);
                            }
                            items.add(item);
                        }
                        return items.parallelStream();
                    })
                    .filter(Objects::nonNull)
                    .collect(toList());
        } catch (Exception e) {
            System.out.println(file);
            throw new RuntimeException(e);
        }
    }

    private static BigDecimal getBigDecimal(Row row, char c) {
        try {
            final Cell cell = Jpoi.cell(row, c);
            final double d = cell.getNumericCellValue();
            return BigDecimal.valueOf(d);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getString(Row row, char c) {
        try {
            final Cell cell = Jpoi.cell(row, c);
            return cell.getStringCellValue();
        } catch (Exception e) {
            return null;
        }
    }
}