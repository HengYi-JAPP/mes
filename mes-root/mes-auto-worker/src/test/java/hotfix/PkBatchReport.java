package hotfix;

import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.Workshop;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author jzb 2019-02-01
 */
public class PkBatchReport {
    public static void main(String[] args) {
        final Workshop workshop = AAReport.Workshops.D;
        final LocalDate startLd = LocalDate.of(2019, 4, 22);
        final LocalDate endLd = LocalDate.of(2019, 4, 22);
        Stream.iterate(startLd, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startLd, endLd) + 1)
                .forEach(ld -> {
                    final List<PackageBox> packageBoxes = AAReport.packageBoxes(workshop, ld);
                    final BigDecimal CHECK_BD = new BigDecimal("7");
                    final long count = packageBoxes.stream()
                            .filter(it -> "GD010515".equals(it.getBatch().getBatchNo()))
                            .filter(it -> it.getGrade().getSortBy() >= 100)
                            .filter(it -> {
                                final double netWeight = it.getNetWeight();
                                final int silkCount = it.getSilkCount();
//                                final BigDecimal bd = BigDecimal.valueOf(netWeight / silkCount);
//                                return !CHECK_BD.equals(bd);

                                final int val = (int) (netWeight / silkCount);
                                return 7 != val;
                            })
                            .peek(it -> {
                                System.out.println(it.getCode());
                            })
                            .count();
                });
    }
}
