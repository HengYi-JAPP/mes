package hotfix;

import com.google.common.collect.ComparisonChain;
import com.hengyi.japp.mes.auto.domain.*;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jzb 2019-02-01
 */
public class PkSapReport {
    public static void main(String[] args) {
        final Workshop workshop = AAReport.Workshops.A;
        AAReport.packageBoxes(workshop, LocalDate.of(2019, 3, 1)).forEach(packageBox -> {
            final String code = packageBox.getCode();
            final Batch batch = packageBox.getBatch();
            final Product product = batch.getProduct();
            final String productName = product.getName();
            final String batchNo = batch.getBatchNo();
            final Grade grade = packageBox.getGrade();
            final String gradeName = grade.getName();
            final int silkCount = packageBox.getSilkCount();
            final double netWeight = packageBox.getNetWeight();
            final String join = String.join("\t", code, productName, batchNo, gradeName, "" + silkCount, "" + netWeight);
            System.out.println(join);
        });

        AAReport.packageBoxes(workshop, LocalDate.of(2019, 2, 13)).parallelStream()
                .collect(Collectors.groupingBy(it -> Pair.of(it.getBatch(), it.getGrade())))
                .entrySet().stream()
                .sorted((o1, o2) -> {
                    final Pair<Batch, Grade> pair1 = o1.getKey();
                    final Pair<Batch, Grade> pair2 = o2.getKey();
                    final Batch batch1 = pair1.getLeft();
                    final Batch batch2 = pair2.getLeft();
                    final Grade grade1 = pair1.getRight();
                    final Grade grade2 = pair2.getRight();
                    return ComparisonChain.start()
                            .compare(batch1.getBatchNo(), batch2.getBatchNo())
                            .compare(grade1.getName(), grade2.getName())
//                            .compare(grade2.getSortBy(), grade1.getSortBy())
                            .result();
                })
                .forEach(entry -> {
                    final Pair<Batch, Grade> pair = entry.getKey();
                    final Batch batch = pair.getLeft();
                    final Grade grade = pair.getRight();
                    final List<PackageBox> list = entry.getValue();
                    final int packageBoxCount = list.size();
                    final BigDecimal silkWeight = list.parallelStream()
                            .map(PackageBox::getNetWeight)
                            .map(BigDecimal::valueOf)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    final String join = String.join("\t", batch.getBatchNo(), grade.getName(), "" + packageBoxCount, "" + silkWeight);
                    System.out.println(join);
                });
    }
}
