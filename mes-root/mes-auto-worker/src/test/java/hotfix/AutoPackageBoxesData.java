package hotfix;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.SaleType;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * @author jzb 2019-01-08
 */
public class AutoPackageBoxesData {
    private final List<PackageBox> packageBoxes;
    private final ForkJoinTask<Collection<Item>> task;

    public AutoPackageBoxesData(List<PackageBox> packageBoxes) {
        this.packageBoxes = packageBoxes;
        final SingleTask singleTask = new SingleTask(packageBoxes);
        task = ForkJoinPool.commonPool().submit(singleTask);
    }

    @SneakyThrows
    public int size() {
        return packageBoxes.size();
    }

    @SneakyThrows
    public Collection<Item> data() {
        return task.get();
    }

    private class SingleTask extends RecursiveTask<Collection<Item>> {
        private final List<PackageBox> _packageBoxes;

        private SingleTask(List<PackageBox> packageBoxes) {
            _packageBoxes = packageBoxes;
        }

        @Override
        protected Collection<Item> compute() {
            final Collection<Item> result = Lists.newArrayList();

            final int size = _packageBoxes.size();
            if (size < 1) {
                return result;
            }

            final PackageBox packageBox = IterableUtils.get(_packageBoxes, 0);
            if (size == 1) {
                final List<Silk> silks = AAReport.packageBoxSilks(packageBox);
                final Item item = new Item(packageBox, silks);
                item.setBatch(packageBox.getBatch());
                item.setGrade(packageBox.getGrade());
                item.setSaleType(packageBox.getSaleType());
                result.add(item);
                return result;
            }

            final List<PackageBox> packageBoxes1 = Lists.newArrayList(packageBox);
            final SingleTask task1 = new SingleTask(packageBoxes1);
            task1.fork();

            final List<PackageBox> packageBoxes2 = _packageBoxes.subList(1, size);
            final SingleTask task2 = new SingleTask(packageBoxes2);
            task2.fork();

            final Collection<Item> join1 = task1.join();
            final Collection<Item> join2 = task2.join();
            result.addAll(join1);
            result.addAll(join2);
            return result;
        }
    }

    @Data
    public class Item {
        private final PackageBox packageBox;
        private final List<Silk> silks;
        private Batch batch;
        private Grade grade;
        private SaleType saleType;

        public Map<Triple<Batch, Line, SaleType>, Integer> toData() {
            final Map<Triple<Batch, Line, SaleType>, Integer> result = Maps.newConcurrentMap();
            silks.parallelStream()
                    .collect(Collectors.groupingBy(silk -> {
                        final LineMachine lineMachine = silk.getLineMachine();
                        final Line line = lineMachine.getLine();
                        return Triple.of(batch, line, saleType);
                    }))
                    .entrySet()
                    .parallelStream()
                    .forEach(entry -> {
                        final Triple<Batch, Line, SaleType> key = entry.getKey();
                        final int value = entry.getValue().size();
                        result.put(key, value);
                    });
            return result;
        }
    }
}
