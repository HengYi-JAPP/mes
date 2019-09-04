package com.hengyi.japp.mes.auto.report.application.dto.statistic;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.poi.Jpoi;
import com.google.common.collect.Lists;
import io.vertx.core.json.JsonArray;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public StatisticReportCombine(Stream<InputStream> inputStreamStream) {
        items = inputStreamStream
                .map(StatisticReportCombine::items)
                .flatMap(Collection::parallelStream)
                .collect(toList());
        items = collect(items).collect(toList());
    }

    public static StatisticReportCombine from(JsonArray jsonArray) {
        final Stream<InputStream> stream = StreamSupport.stream(jsonArray.spliterator(), false)
                .map(it -> {
                    if (it instanceof byte[]) {
                        return (byte[]) it;
                    }
                    final String s = (String) it;
                    return Base64.getDecoder().decode(s);
                })
                .map(ByteArrayInputStream::new);
        return new StatisticReportCombine(stream);
    }

    public static StatisticReportCombine from(Collection<File> files) {
        final Stream<InputStream> fileInputStreamStream = J.emptyIfNull(files).stream()
                .map(it -> {
                    try {
                        return new FileInputStream(it);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
        return new StatisticReportCombine(fileInputStreamStream);
    }

    private static Stream<Item> collect(Collection<Item> items) {
        return items.parallelStream()
                .collect(groupingBy(it -> {
                    final Item item = new Item(it.isBigSilkCar(), it.getLine(), it.getBatch(), it.getGrade());
                    final Item item2 = new Item(it.isBigSilkCar(), it.getLine(), it.getBatch(), it.getGrade());
                    return item;
                }))
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

    @SneakyThrows
    private static Collection<Item> items(InputStream is) {
        try (is) {
            @Cleanup final Workbook wb = WorkbookFactory.create(is);
            final Sheet sheet = wb.getSheetAt(0);
            return IntStream.rangeClosed(sheet.getFirstRowNum(), sheet.getLastRowNum())
                    .mapToObj(sheet::getRow)
                    .filter(row -> Stream.of('A', 'B', 'C', 'D').map(it -> getString(row, it)).allMatch(J::nonBlank)
                            && Stream.of('E', 'F', 'G', 'H', 'I').map(it -> getBigDecimal(row, it)).allMatch(Objects::nonNull)
                    )
                    .map(StatisticReportCombine::items)
                    .flatMap(Collection::parallelStream)
                    .filter(Objects::nonNull)
                    .collect(toList());
        }
    }

    private static Collection<Item> items(Row row) {
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

        final Collection<Item> items = Lists.newArrayList();
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
            item.setSilkWeight(aWeight);
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
            item.setSilkWeight(bWeight);
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
            item.setSilkWeight(cWeight);
            if (!silkCountAdded) {
                item.setSilkCount(silkCount);
            }
            items.add(item);
        }
        return items;
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
            return StringUtils.trim(cell.getStringCellValue());
        } catch (Exception e) {
            return null;
        }
    }
}