package com.hengyi.japp.mes.auto.application.report;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jzb 2018-08-12
 */
@Data
public class MeasurePackageBoxReport implements Serializable {
    private final Collection<Item> items;

    public MeasurePackageBoxReport(Collection<PackageBox> packageBoxes) {
        items = J.emptyIfNull(packageBoxes).parallelStream()
                .collect(Collectors.groupingBy(PackageBox::getBatch))
                .entrySet().parallelStream()
                .map(entry -> {
                    final Batch batch = entry.getKey();
                    final List<PackageBox> packageBoxList = entry.getValue();
                    return new Item(batch, packageBoxList);
                })
                .collect(Collectors.toList());
    }

    @Data
    public static class Item implements Serializable {
        private final Batch batch;
        private final Collection<PackageBox> packageBoxes;
    }
}
