package hotfix;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.poi.Jpoi;
import com.google.common.collect.ComparisonChain;
import com.hengyi.japp.mes.auto.application.report.StatisticsReportDay;
import com.hengyi.japp.mes.auto.domain.*;
import lombok.Cleanup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

/**
 * @author jzb 2019-01-08
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AAReportExcel extends RecursiveAction {
    private final LocalDate ld;
    private Collection<PackageClassTask> tasks;

    @Override
    protected void compute() {
        tasks = AAReport.packageClasses
                .parallelStream()
                .map(PackageClassTask::new)
                .collect(Collectors.toList());
        invokeAll(tasks);
    }

    public void printDetail() {
        tasks.stream().filter(it -> J.nonEmpty(it.packageBoxes)).forEach(task -> {
            System.out.println("=====" + ld + "[" + task.packageClass.getName() + "]=====");

            task.diffCalcus.stream().sorted().forEach(diffCalcu -> {
                J.emptyIfNull(diffCalcu.items()).stream().forEach(item -> {
                    final Line line = item.getLine();
                    final String join = String.join("\t", item.getBatch().getBatchNo(), line.getName(),
                            "" + item.getSilkCount(), "" + item.getNetWeight());
                    System.out.println(join);
                });

                final String join = String.join("\t", diffCalcu.batch.getBatchNo(), diffCalcu.grade.getName(),
                        "" + diffCalcu.getSilkCount(), "" + diffCalcu.getNetWeight());
                System.out.println(join);
                System.out.println("-------------------------------");
            });
        });
    }

    public void printByDay() {
        System.out.println("=====" + ld + "=====");
        tasks.stream()
                .flatMap(it -> {
                    final List<DiffCalcu> diffCalcus = it.getDiffCalcus();
                    return diffCalcus.stream();
                })
                .collect(Collectors.groupingBy(it -> Pair.of(it.batch, it.grade)))
                .entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    final Pair<Batch, Grade> key1 = o1.getKey();
                    final Batch batch1 = key1.getKey();
                    final Grade grade1 = key1.getRight();
                    final Pair<Batch, Grade> key2 = o2.getKey();
                    final Batch batch2 = key2.getKey();
                    final Grade grade2 = key2.getRight();
                    return ComparisonChain.start()
                            .compare(batch1.getBatchNo(), batch2.getBatchNo())
                            .compare(grade1.getSortBy(), grade2.getSortBy())
                            .result();
                })
                .forEach(entry -> {
                    final Pair<Batch, Grade> key = entry.getKey();
                    final List<DiffCalcu> diffCalcus = entry.getValue();
                    final int silkCount = diffCalcus.parallelStream().mapToInt(DiffCalcu::getSilkCount).sum();
                    final double netWeight = diffCalcus.parallelStream().mapToDouble(DiffCalcu::getNetWeight).sum();

                    final String join = String.join("\t", key.getLeft().getBatchNo(), key.getRight().getName(),
                            "" + silkCount, "" + netWeight);
                    System.out.println(join);
                    System.out.println("-------------------------------");
                });
    }

    @SneakyThrows
    public void toExcel() {
        System.out.println("=====" + ld + "=====");

        @Cleanup final Workbook wb = new XSSFWorkbook();
        final Sheet sheet = wb.createSheet();
        Cell cell = Jpoi.cell(sheet, 0, 0);
        cell.setCellValue("线别");
        cell = Jpoi.cell(sheet, 0, 1);
        cell.setCellValue("品名");
        cell = Jpoi.cell(sheet, 0, 2);
        cell.setCellValue("规格");
        cell = Jpoi.cell(sheet, 0, 3);
        cell.setCellValue("批号");
        cell = Jpoi.cell(sheet, 0, 4);
        cell.setCellValue("AA");
        cell = Jpoi.cell(sheet, 0, 5);
        cell.setCellValue("B");
        cell = Jpoi.cell(sheet, 0, 6);
        cell.setCellValue("C");

        final List<StatisticsReportDay.XlsxItem> xlsxItems = tasks.stream().filter(it -> J.nonEmpty(it.packageBoxes))
                .flatMap(task -> task.diffCalcus.stream()
                        .flatMap(diffCalcu -> J.emptyIfNull(diffCalcu.items()).stream())
                )
                .collect(Collectors.groupingBy(it -> {
                    final Batch batch = it.getBatch();
                    final Line line = it.getLine();
                    final Grade grade = it.getGrade();
                    return Triple.of(line, batch, grade);
                }))
                .entrySet()
                .stream()
                .map(entry -> {
                    final AAReportItem item = new AAReportItem();
                    final Triple<Line, Batch, Grade> triple = entry.getKey();
                    final List<AAReportItem> value = entry.getValue();
                    item.setLine(triple.getLeft());
                    item.setBatch(triple.getMiddle());
                    item.setGrade(triple.getRight());
                    final int silkCount = value.parallelStream().mapToInt(AAReportItem::getSilkCount).sum();
                    item.setSilkCount(silkCount);
                    final double netWeight = value.parallelStream()
                            .mapToDouble(AAReportItem::getNetWeight)
                            .sum();
                    item.setNetWeight(netWeight);
                    return item;
                })
                .collect(Collectors.groupingBy(it -> Pair.of(it.getLine(), it.getBatch())))
                .entrySet().stream()
                .map(entry -> {
                    final Pair<Line, Batch> pair = entry.getKey();
                    final Line line = pair.getLeft();
                    final Batch batch = pair.getRight();
                    final StatisticsReportDay.XlsxItem xlsxItem = new StatisticsReportDay.XlsxItem(line, batch);
                    J.emptyIfNull(entry.getValue()).forEach(it -> {
                        final Grade grade = it.getGrade();
                        final Pair<Integer, BigDecimal> value = Pair.of(it.getSilkCount(), BigDecimal.valueOf(it.getNetWeight()));
                        xlsxItem.getGradePairMap().put(grade, value);
                    });
                    return xlsxItem;
                })
                .collect(Collectors.toList());
//                .sorted()
//                .forEach(item -> {
//                    final Line line = item.getLine();
//                    final String join = String.join("\t", line.getName(), item.getBatch().getBatchNo(), item.getGrade().getName(),
//                            "" + item.getSilkCount(), "" + item.getNetWeight());
//                    System.out.println(join);
//                });

        int rowIndex = 1;
        for (StatisticsReportDay.XlsxItem item : J.emptyIfNull(xlsxItems)) {
            final Line line = item.getLine();
            final Batch batch = item.getBatch();
            final Product product = batch.getProduct();
            cell = Jpoi.cell(sheet, rowIndex, 0);
            cell.setCellValue(line.getName());
            cell = Jpoi.cell(sheet, rowIndex, 1);
            cell.setCellValue(product.getName());
            cell = Jpoi.cell(sheet, rowIndex, 2);
            cell.setCellValue(batch.getSpec());
            cell = Jpoi.cell(sheet, rowIndex, 3);
            cell.setCellValue(batch.getBatchNo());
            for (Grade grade : item.getGradePairMap().keySet()) {
                final Pair<Integer, BigDecimal> pair = item.getGradePairMap().get(grade);
                if ("AA".equals(grade.getName())) {
                    cell = Jpoi.cell(sheet, rowIndex, 4);
                    cell.setCellValue(pair.getRight().toString());
                } else if ("A".equals(grade.getName())) {
                    cell = Jpoi.cell(sheet, rowIndex, 5);
                    cell.setCellValue(pair.getRight().toString());
                } else if ("B".equals(grade.getName())) {
                    cell = Jpoi.cell(sheet, rowIndex, 6);
                    cell.setCellValue(pair.getRight().toString());
                } else if ("C".equals(grade.getName())) {
                    cell = Jpoi.cell(sheet, rowIndex, 7);
                    cell.setCellValue(pair.getRight().toString());
                }
            }
            rowIndex++;
        }
        @Cleanup final FileOutputStream fileOutputStream = new FileOutputStream("/home/jzb/test.xlsx");
        wb.write(fileOutputStream);
//        @Cleanup final ByteArrayOutputStream os = new ByteArrayOutputStream();
//        wb.write(os);
    }

    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    private class PackageClassTask extends RecursiveAction {
        private final PackageClass packageClass;
        private List<PackageBox> packageBoxes;
        private List<DiffCalcu> diffCalcus;

        @Override
        protected void compute() {
            packageBoxes = AAReport.packageBoxes(ld, packageClass);
            diffCalcus = packageBoxes.parallelStream()
                    .collect(Collectors.groupingBy(it -> Pair.of(it.getBatch(), it.getGrade())))
                    .entrySet()
                    .parallelStream()
                    .map(entry -> {
                        final Pair<Batch, Grade> key = entry.getKey();
                        final Batch batch = key.getLeft();
                        final Grade grade = key.getRight();
                        if (grade.getSortBy() >= 100) {
                            return new AADiffCalcu(batch, grade, entry.getValue());
                        } else {
                            return new ABCDiffCalcu(batch, grade, entry.getValue());
                        }
                    })
                    .collect(Collectors.toList());
            invokeAll(diffCalcus);
        }
    }

}