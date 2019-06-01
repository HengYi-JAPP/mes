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
        final LocalDate startLd = LocalDate.of(2019, 5, 20);
        final LocalDate endLd = LocalDate.of(2019, 5, 26);
        Stream.iterate(startLd, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startLd, endLd) + 1)
                .forEach(ld -> {
                    final List<PackageBox> packageBoxes = AAReport.packageBoxes(workshop, ld);
                    packageBoxes.stream()
                            .filter(it -> it.getGrade().getSortBy() >= 100)
                            .filter(it -> {
                                final BigDecimal CHECK_BD = BigDecimal.valueOf(it.getBatch().getSilkWeight());
                                final double netWeight = it.getNetWeight();
                                final int silkCount = it.getSilkCount();
                                final BigDecimal bd = BigDecimal.valueOf(netWeight / silkCount);

                                final double v = CHECK_BD.subtract(bd).doubleValue();
                                return v > 0;
                            })
                            .peek(it -> System.out.println(it.getCode()))
                            .count();
                });
    }
}
