package hotfix;

import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Line;
import lombok.Data;

/**
 * @author jzb 2019-01-09
 */
@Data
public class AAReportItem {
    private Line line;
    private Batch batch;
    private int packageBoxCount;
    private int silkCount;
    private int netWeight;

    private int autoSilkCount1;
    private int autoSilkCount2;

    @Override
    public String toString() {
        return line.getName() + "\t" + batch.getBatchNo() + "\t" + packageBoxCount + "\t" + silkCount + "\t" + (silkCount * 8);
    }
}
