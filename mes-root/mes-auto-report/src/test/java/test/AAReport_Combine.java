package test;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.poi.Jpoi;
import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-05-31
 */
@Slf4j
public class AAReport_Combine {

    //    @SneakyThrows
    public static void main(String[] args) {
//        final List<XlsxItem> xlsxItems = IntStream.rangeClosed(1, 6).parallel()
//                .mapToObj(it -> "/home/jzb/D/" + it + ".xlsx")
//                .map(AAReport_Combine::test)
//                .flatMap(Collection::stream)
//                .collect(groupingBy(it -> Pair.of(it.getLineNo(), it.getBatchNo())))
//                .values().parallelStream()
//                .map(AAReport_Combine::combine)
//                .collect(groupingBy(Item::getLineNo))
//                .entrySet().parallelStream()
//                .map(entry -> {
//                    final String lineName = entry.getKey();
//                    final LineDTO line = new LineDTO();
//                    line.setId(lineName);
//                    line.setName(lineName);
//                    final Multimap<BatchDTO, Triple<GradeDTO, Integer, BigDecimal>> batchMultimap = HashMultimap.create();
//                    entry.getValue().forEach(item -> {
//                        final BatchDTO batch = new BatchDTO();
//                        batch.setId(item.batchNo);
//                        batch.setBatchNo(item.batchNo);
//                        batch.setSpec(item.spec);
//                        final ProductDTO product = new ProductDTO();
//                        batch.setProduct(product);
//                        product.setId(item.productName);
//                        product.setName(item.productName);
//
//                        final GradeDTO aaGrade = new GradeDTO();
//                        aaGrade.setId("AA");
//                        aaGrade.setName("AA");
//                        batchMultimap.put(batch, Triple.of(aaGrade, item.silkCount, item.aaWeight));
//
//                        final GradeDTO aGrade = new GradeDTO();
//                        aGrade.setId("A");
//                        aGrade.setName("A");
//                        batchMultimap.put(batch, Triple.of(aGrade, item.silkCount, item.aWeight));
//
//                        final GradeDTO bGrade = new GradeDTO();
//                        bGrade.setId("B");
//                        bGrade.setName("B");
//                        batchMultimap.put(batch, Triple.of(bGrade, item.silkCount, item.bWeight));
//
//                        final GradeDTO cGrade = new GradeDTO();
//                        cGrade.setId("C");
//                        cGrade.setName("C");
//                        batchMultimap.put(batch, Triple.of(cGrade, item.silkCount, item.cWeight));
//                    });
//
//                    return new XlsxItem(false,line, batchMultimap);
//                })
//                .collect(toList());
//        @Cleanup final Workbook wb = new XSSFWorkbook();
//        PoiUtil.fillSheet1(wb.createSheet(), xlsxItems);
//        @Cleanup final FileOutputStream os = new FileOutputStream("/home/jzb/test.xlsx");
//        wb.write(os);
    }

    private static Item combine(List<Item> items) {
        final Item copyItem = items.get(0);
        final BigDecimal aaWeight = items.parallelStream().map(Item::getAaWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal aWeight = items.parallelStream().map(Item::getAWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal bWeight = items.parallelStream().map(Item::getBWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal cWeight = items.parallelStream().map(Item::getCWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        final int silkCount = items.parallelStream().collect(summingInt(Item::getSilkCount));
        return new Item(copyItem.lineNo, copyItem.productName, copyItem.batchNo, copyItem.spec, aaWeight, aWeight, bWeight, cWeight, silkCount);
    }

    @SneakyThrows
    private static Collection<Item> test(String path) {
        @Cleanup final Workbook wb = WorkbookFactory.create(new File(path));
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
                .map(row -> {
                    final String lineNo = getString(row, 'A');
                    final String productName = getString(row, 'B');
                    final String spec = getString(row, 'C');
                    final String batchNo = getString(row, 'D');
                    final BigDecimal aaWeight = getBigDecimal(row, 'E');
                    final BigDecimal aWeight = getBigDecimal(row, 'F');
                    final BigDecimal bWeight = getBigDecimal(row, 'G');
                    final BigDecimal cWeight = getBigDecimal(row, 'H');
                    final int silkCount = getBigDecimal(row, 'J').intValue();
                    return new Item(lineNo, productName, batchNo, spec, aaWeight, aWeight, bWeight, cWeight, silkCount);
                })
                .filter(Objects::nonNull)
                .collect(toList());
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

    @Data
    public static class Item implements Serializable {
        private final String lineNo;
        private final String productName;
        private final String batchNo;
        private final String spec;
        private final BigDecimal aaWeight;
        private final BigDecimal aWeight;
        private final BigDecimal bWeight;
        private final BigDecimal cWeight;
        private final int silkCount;
    }

}
