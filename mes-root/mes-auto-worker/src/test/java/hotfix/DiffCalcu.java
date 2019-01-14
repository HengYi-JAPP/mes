package hotfix;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.Silk;
import hotfix.AAReport.AAReportSilkCarRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * @author jzb 2019-01-09
 */
public abstract class DiffCalcu extends RecursiveAction implements Comparable<DiffCalcu> {
    protected final Batch batch;
    protected final Grade grade;
    protected final List<PackageBox> packageBoxes;
    protected final List<AutoData> autoData = Lists.newArrayList();
    protected final List<ManualData> manualData = Lists.newArrayList();
    protected final List<ManualAppendData> manualAppendData = Lists.newArrayList();
    private final List<RecursiveAction> recursiveActions = Lists.newArrayList();

    protected DiffCalcu(Batch batch, Grade grade, List<PackageBox> packageBoxes) {
        this.batch = batch;
        this.grade = grade;
        this.packageBoxes = packageBoxes;

        packageBoxes.forEach(packageBox -> {
            final RecursiveAction action;
            switch (packageBox.getType()) {
                case AUTO: {
                    final AutoData task = new AutoData(packageBox);
                    autoData.add(task);
                    action = task;
                    break;
                }
                case MANUAL: {
                    final ManualData task = new ManualData(packageBox);
                    manualData.add(task);
                    action = task;
                    break;
                }
                case MANUAL_APPEND: {
                    final ManualAppendData task = new ManualAppendData(packageBox);
                    manualAppendData.add(task);
                    action = task;
                    break;
                }
                default: {
                    throw new RuntimeException();
                }
            }
            recursiveActions.add(action);
        });
    }

    @Override
    protected void compute() {
        invokeAll(recursiveActions);
        subCompute();
    }

    protected abstract void subCompute();

    protected abstract List<AAReportItem> items();

    @Override
    public int compareTo(DiffCalcu o) {
        return ComparisonChain.start()
                .compare(batch.getBatchNo(), o.batch.getBatchNo())
                .compare(o.grade.getSortBy(), grade.getSortBy())
                .result();
    }

    public int getPackageBoxCount() {
        return packageBoxes.size();
    }

    public int getSilkCount() {
        return packageBoxes
                .parallelStream()
                .mapToInt(PackageBox::getSilkCount)
                .sum();
    }

    public double getNetWeight() {
        return packageBoxes
                .parallelStream()
                .mapToDouble(PackageBox::getNetWeight)
                .sum();
    }

    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class AutoData extends RecursiveAction {
        private final PackageBox packageBox;
        private List<Silk> silks;

        @Override
        protected void compute() {
            silks = AAReport.packageBoxSilks(packageBox);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class ManualData extends RecursiveAction {
        private final PackageBox packageBox;
        private List<AAReportSilkCarRecord> silkCarRecords;

        @Override
        protected void compute() {
            silkCarRecords = AAReport.packageBoxSilkCarRecords(packageBox);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class ManualAppendData extends RecursiveAction {
        private final PackageBox packageBox;

        @Override
        protected void compute() {
        }
    }

}
