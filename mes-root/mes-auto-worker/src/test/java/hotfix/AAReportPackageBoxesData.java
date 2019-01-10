package hotfix;

import com.hengyi.japp.mes.auto.domain.PackageBox;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jzb 2019-01-08
 */
@Data
public class AAReportPackageBoxesData {
    private final List<PackageBox> packageBoxes;
    private AutoPackageBoxesData autoPackageBoxesData = new AutoPackageBoxesData(Collections.EMPTY_LIST);
    private ManualPackageBoxesData manualPackageBoxesData = new ManualPackageBoxesData(Collections.EMPTY_LIST);
    private ManualAppendPackageBoxesData manualAppendPackageBoxesData = new ManualAppendPackageBoxesData(Collections.EMPTY_LIST);

    public AAReportPackageBoxesData(List<PackageBox> packageBoxes) {
        this.packageBoxes = packageBoxes;

        packageBoxes.parallelStream()
                .collect(Collectors.groupingBy(PackageBox::getType))
                .entrySet()
                .forEach(entry -> {
                    final List<PackageBox> value = entry.getValue();
                    switch (entry.getKey()) {
                        case AUTO: {
                            autoPackageBoxesData = new AutoPackageBoxesData(value);
                            return;
                        }
                        case MANUAL: {
                            manualPackageBoxesData = new ManualPackageBoxesData(value);
                            return;
                        }
                        case MANUAL_APPEND: {
                            manualAppendPackageBoxesData = new ManualAppendPackageBoxesData(value);
                            return;
                        }
                    }
                });
    }
}
