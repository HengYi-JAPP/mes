package hotfix;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.application.report.StatisticsReportDay_Batch;
import com.hengyi.japp.mes.auto.domain.*;
import hotfix.AAReport.AAReportSilkCarRecord;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * @author jzb 2019-01-08
 */
@Slf4j
@Data
public class AAReportDay_Batch extends StatisticsReportDay_Batch {

    public AAReportDay_Batch(Batch batch, Collection<PackageBox> packageBoxes) {
        super(batch, packageBoxes);
    }

    @Override
    protected void collectToLine(PackageBox packageBox) {
        final Grade grade = packageBox.getGrade();
        final Collection<Silk> silks = AAReport.packageBoxSilks(packageBox);
        if (J.nonEmpty(silks)) {
            silks.parallelStream().forEach(silk -> collectToLine(grade, silk));
            return;
        }
        final Collection<AAReportSilkCarRecord> silkCarRecords = AAReport.packageBoxSilkCarRecords(packageBox);
        J.emptyIfNull(silkCarRecords).parallelStream()
                .map(AAReportSilkCarRecord::getInitSilks)
                .filter(J::nonEmpty)
                .flatMap(Collection::parallelStream)
                .map(SilkRuntime::getSilk)
                .forEach(silk -> collectToLine(grade, silk));
    }

}
