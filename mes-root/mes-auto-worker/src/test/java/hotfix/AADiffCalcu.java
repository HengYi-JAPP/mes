package hotfix;

import com.hengyi.japp.mes.auto.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jzb 2019-01-09
 */
@Slf4j
public class AADiffCalcu extends DiffCalcu {
    private List<AAReportItem> items;

    public AADiffCalcu(Batch batch, Grade grade, List<PackageBox> packageBoxes) {
        super(batch, grade, packageBoxes);
    }

    @Override
    protected void subCompute() {
        final Stream<Silk> silkStream1 = autoData.parallelStream()
                .map(AutoData::getSilks)
                .flatMap(Collection::parallelStream);
        final Stream<Silk> silkStream2 = manualData.parallelStream()
                .map(ManualData::getSilkCarRecords)
                .flatMap(Collection::parallelStream)
                .flatMap(it -> {
                    final Collection<SilkRuntime> initSilks = it.getInitSilks();
                    return initSilks.parallelStream();
                })
                .map(SilkRuntime::getSilk);
        items = Stream.concat(silkStream1, silkStream2)
                .collect(Collectors.toMap(it -> it.getLineMachine().getLine(), it -> 1, Integer::sum))
                .entrySet()
                .parallelStream()
                .map(entry -> {
                    final AAReportItem item = new AAReportItem();
                    final Line line = entry.getKey();
                    final Integer silkCount = entry.getValue();
                    item.setLine(line);
                    item.setBatch(batch);
                    item.setGrade(grade);
                    item.setSilkCount(silkCount);
                    return item;
                })
                .collect(Collectors.toList());
        diff();
    }

    @Override
    protected List<AAReportItem> items() {
        return items;
    }

    private void diff() {
        if (items.size() < 1) {
            final int silkCount = packageBoxes.parallelStream().mapToInt(PackageBox::getSilkCount).sum();
            final double netWeight = packageBoxes.parallelStream().mapToDouble(PackageBox::getNetWeight).sum();
            final String join = String.join("\t", "error:", batch.getBatchNo(), grade.getName(), "" + silkCount, "" + netWeight);
            log.error(join);
            return;
        }

        final int PACKAGE_BOX_COUNT = getPackageBoxCount();
        final int SILK_COUNT = getSilkCount();
        if (items.size() == 1) {
            final AAReportItem item = IterableUtils.get(items, 0);
            item.setPackageBoxCount(PACKAGE_BOX_COUNT);
            item.setSilkCount(SILK_COUNT);
            return;
        }

        final int itemsSumSilkCount = items.parallelStream()
                .mapToInt(AAReportItem::getSilkCount)
                .sum();
        final boolean sameSilkCount = SILK_COUNT == itemsSumSilkCount;

        items.forEach(item -> {
            final int i = item.getSilkCount();
            final int packageBoxCount = PACKAGE_BOX_COUNT * i / itemsSumSilkCount;
            item.setPackageBoxCount(packageBoxCount);
        });
        diffPackageBoxCount(PACKAGE_BOX_COUNT, items);

        if (!sameSilkCount) {
            items.forEach(item -> {
                final int i = item.getSilkCount();
                final int silkCount = SILK_COUNT * i / itemsSumSilkCount;
                item.setSilkCount(silkCount);
            });
            diffSilkCount(SILK_COUNT, items);
        }
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

}
