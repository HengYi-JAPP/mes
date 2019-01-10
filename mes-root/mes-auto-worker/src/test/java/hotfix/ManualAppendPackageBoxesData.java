package hotfix;

import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.data.SaleType;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jzb 2019-01-08
 */
@Data
class ManualAppendPackageBoxesData {
    private final List<PackageBox> packageBoxes;

    public ManualAppendPackageBoxesData(List<PackageBox> packageBoxes) {
        this.packageBoxes = packageBoxes;
    }

    @SneakyThrows
    public int size() {
        return packageBoxes.size();
    }

    @SneakyThrows
    public Collection<Item> data() {
        return packageBoxes.parallelStream()
                .map(packageBox -> {
                    final Item item = new Item(packageBox);
                    item.setBatch(packageBox.getBatch());
                    item.setGrade(packageBox.getGrade());
                    item.setSaleType(packageBox.getSaleType());
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Data
    public class Item {
        private final PackageBox packageBox;
        private Batch batch;
        private Grade grade;
        private SaleType saleType;

        public Map<Pair<Batch, SaleType>, Integer> toData() {
            final Map<Pair<Batch, SaleType>, Integer> result = Maps.newConcurrentMap();
            final Pair<Batch, SaleType> pair = Pair.of(batch, saleType);
            result.put(pair, packageBox.getSilkCount());
            return result;
        }
    }
}
