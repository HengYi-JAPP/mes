package hotfix;

import com.hengyi.japp.mes.auto.application.report.StatisticsReport;
import com.hengyi.japp.mes.auto.application.report.StatisticsReportDay;
import com.hengyi.japp.mes.auto.domain.PackageBox;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author jzb 2019-01-08
 */
public class AAReportDay extends StatisticsReportDay {

    public AAReportDay(LocalDate ld) {
        super(null, ld, AAReport.packageBoxes(ld));
    }

    @Override
    protected Collection<StatisticsReport.Item> calcItems() {
        return getPackageBoxes().parallelStream()
                .collect(Collectors.groupingBy(PackageBox::getBatch))
                .entrySet().parallelStream()
                .map(entry -> new AAReportDay_Batch(entry.getKey(), entry.getValue()))
                .flatMap(AAReportDay_Batch::lineDiff)
                .collect(Collectors.toList());
    }
}
