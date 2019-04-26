package com.hengyi.japp.mes.auto.application.report;

import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author jzb 2018-08-12
 */
@Data
public class MeasureFiberReport implements Serializable {
//    private final Collection<Item> items;

//    public MeasureFiberReport(Collection<PackageBox> packageBoxes) {
//        items = J.emptyIfNull(packageBoxes).parallelStream()
//                .collect(Collectors.groupingBy(PackageBox::getBatch))
//                .entrySet().parallelStream()
//                .map(entry -> {
//                    final Batch batch = entry.getKey();
//                    final List<PackageBox> packageBoxList = entry.getValue();
//                    return new Item(batch, packageBoxList);
//                })
//                .collect(Collectors.toList());
//    }

    @Data
    public static class Item implements Serializable {
        private final List<EventSource> eventSources;
        private final SilkCarRecord silkCarRecord;
//        private final Operator operator;
//        private final Product product;
//        private final Collection<SilkCarRecord> silkCarRecords;
//        private final Collection<Silk> silks;
    }
}
