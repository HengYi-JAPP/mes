package hotfix;

import com.google.common.collect.ComparisonChain;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.Line;
import lombok.Data;

/**
 * @author jzb 2019-01-09
 */
@Data
public class AAReportItem implements Comparable<AAReportItem> {
    private Line line;
    private Batch batch;
    private Grade grade;

    private int packageBoxCount;
    private int silkCount;
    private double netWeight;

    public double getNetWeight() {
        if (grade.getSortBy() >= 100) {
            return silkCount * batch.getSilkWeight();
        }
        return netWeight;
    }

    @Override
    public String toString() {
        return String.join("\t", "" + line.getName(), "" + batch.getBatchNo(), "" + grade.getName(),
                "" + packageBoxCount, "" + silkCount, "" + netWeight);
    }

    @Override
    public int compareTo(AAReportItem o) {
        return ComparisonChain.start()
                .compare(batch.getBatchNo(), o.batch.getBatchNo())
                .compare(line.getName(), o.line.getName())
                .result();
    }
}
