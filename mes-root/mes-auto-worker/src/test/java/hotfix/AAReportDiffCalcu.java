package hotfix;

import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import org.apache.commons.collections4.IterableUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jzb 2019-01-09
 */
public class AAReportDiffCalcu {
    private Map<Batch, List<PackageBox>> batchActual;

    public AAReportDiffCalcu(List<PackageBox> packageBoxes) {
        batchActual = packageBoxes.parallelStream()
                .collect(Collectors.groupingBy(PackageBox::getBatch));
    }

    public void exe(Collection<AAReportItem> items) {
        batchActual.forEach((batch, packageBoxes) -> {
            final int SILK_COUNT = packageBoxes.stream()
                    .mapToInt(PackageBox::getSilkCount)
                    .sum();
//            System.out.println(batch.getBatchNo() + "\t" + packageBoxes.size() + "\t" + SILK_COUNT);

            final List<AAReportItem> relateItems = items.parallelStream()
                    .filter(it -> batch.equals(it.getBatch()))
                    .collect(Collectors.toList());
            calcu(batch, packageBoxes, relateItems);
        });
//        System.out.println("-------------------------------");
    }

    private void calcu(Batch batch, List<PackageBox> packageBoxes, Collection<AAReportItem> items) {
        if (items.size() < 1) {
            return;
        }

        final int PACKAGE_BOX_COUNT = packageBoxes.size();
        final int SILK_COUNT = packageBoxes.stream()
                .mapToInt(PackageBox::getSilkCount)
                .sum();
        if (items.size() == 1) {
            final AAReportItem item = IterableUtils.get(items, 0);
            item.setPackageBoxCount(PACKAGE_BOX_COUNT);
            item.setSilkCount(SILK_COUNT);
            return;
        }

        final int itemsSumSilkCount = items.parallelStream()
                .mapToInt(it -> it.getAutoSilkCount1() + it.getAutoSilkCount2())
                .sum();
        final boolean sameSilkCount = SILK_COUNT == itemsSumSilkCount;

        items.forEach(item -> {
            final int i = item.getAutoSilkCount1() + item.getAutoSilkCount2();
            final int packageBoxCount = PACKAGE_BOX_COUNT * i / itemsSumSilkCount;
            item.setPackageBoxCount(packageBoxCount);
        });
        diffPackageBoxCount(PACKAGE_BOX_COUNT, items);

        if (sameSilkCount) {
            items.forEach(item -> {
                final int silkCount = item.getAutoSilkCount1() + item.getAutoSilkCount2();
                item.setSilkCount(silkCount);
            });
        } else {
            items.forEach(item -> {
                final int i = item.getAutoSilkCount1() + item.getAutoSilkCount2();
                final int silkCount = SILK_COUNT * i / itemsSumSilkCount;
                item.setSilkCount(silkCount);
            });
            diffSilkCount(SILK_COUNT, items);
        }
        items.forEach(it -> {
            final int silkCount = it.getSilkCount();
            it.setNetWeight(silkCount * 8);
        });
    }

    private void diffPackageBoxCount(final int PACKAGE_BOX_COUNT, Collection<AAReportItem> items) {
        final int sum = items.parallelStream()
                .mapToInt(AAReportItem::getPackageBoxCount)
                .sum();
        if (PACKAGE_BOX_COUNT == sum) {
            return;
        }
        final int diff = PACKAGE_BOX_COUNT - sum;
        final AAReportItem item = IterableUtils.get(items, 0);
        final int packageBoxCount = item.getPackageBoxCount() + diff;
        item.setPackageBoxCount(packageBoxCount);
    }

    private void diffSilkCount(final int SILK_COUNT, Collection<AAReportItem> items) {
        final int sum = items.parallelStream()
                .mapToInt(AAReportItem::getSilkCount)
                .sum();
        if (SILK_COUNT == sum) {
            return;
        }
        final int diff = SILK_COUNT - sum;
        final AAReportItem item = IterableUtils.get(items, 0);
        final int silkCount = item.getSilkCount() + diff;
        item.setSilkCount(silkCount);
    }

}
