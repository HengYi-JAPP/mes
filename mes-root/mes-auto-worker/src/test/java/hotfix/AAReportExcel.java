package hotfix;

import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.PackageClass;
import com.hengyi.japp.mes.auto.domain.data.SaleType;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hotfix.AAReport.gradeAA;

/**
 * @author jzb 2019-01-08
 */
@Data
public class AAReportExcel {
    private final LocalDate ld;
    private final PackageClass packageClass;

    private List<PackageBox> packageBoxes;
    private AAReportPackageBoxesData data;

    public void toExcel() {
        packageBoxes = AAReport.packageBoxes(ld, packageClass)
                .parallelStream()
                .filter(it -> gradeAA.equals(it.getGrade()))
                .collect(Collectors.toList());
        data = new AAReportPackageBoxesData(packageBoxes);

        final Collection<AutoPackageBoxesData.Item> autoData = data.getAutoPackageBoxesData().data();
        final Collection<AutoPackageBoxesData.Item> autoDataAA = autoData.parallelStream()
                .filter(it -> gradeAA.equals(it.getGrade()))
                .collect(Collectors.toList());
        final Collection<ManualPackageBoxesData.Item> manualData = data.getManualPackageBoxesData().data();
        final Collection<ManualPackageBoxesData.Item> manualDataAA = manualData.parallelStream()
                .filter(it -> gradeAA.equals(it.getGrade()))
                .collect(Collectors.toList());
        final Collection<ManualAppendPackageBoxesData.Item> appendData = data.getManualAppendPackageBoxesData().data();
        final Collection<ManualAppendPackageBoxesData.Item> appendDataAA = appendData.parallelStream()
                .filter(it -> gradeAA.equals(it.getGrade()))
                .collect(Collectors.toList());

        final Map<Triple<Batch, Line, SaleType>, Integer> tripleMap = Maps.newConcurrentMap();
        Stream.concat(
                autoDataAA.parallelStream().map(it -> it.toData()),
                manualDataAA.parallelStream().map(it -> it.toData())
        ).flatMap(it -> it.entrySet().parallelStream()).forEach(entry -> {
            final Triple<Batch, Line, SaleType> key = entry.getKey();
            final Integer count = entry.getValue();
            tripleMap.compute(key, (k, v) -> {
                if (v == null) {
                    return count;
                }
                return count + v;
            });
        });

        final Map<Pair<Batch, Line>, AAReportItem> resultMap = Maps.newConcurrentMap();
        tripleMap.entrySet().parallelStream().forEach(entry -> {
            final Triple<Batch, Line, SaleType> triple = entry.getKey();
            final Batch batch = triple.getLeft();
            final Line line = triple.getMiddle();
            final Pair<Batch, Line> key = Pair.of(batch, line);
            final Integer count = entry.getValue();
            switch (triple.getRight()) {
                case DOMESTIC: {
                    resultMap.compute(key, (k, v) -> {
                        if (v == null) {
                            v = new AAReportItem();
                            v.setLine(line);
                            v.setBatch(batch);
                        }
                        v.setAutoSilkCount1(count);
                        return v;
                    });
                    return;
                }
                case FOREIGN: {
                    resultMap.compute(key, (k, v) -> {
                        if (v == null) {
                            v = new AAReportItem();
                            v.setLine(line);
                            v.setBatch(batch);
                        }
                        v.setAutoSilkCount2(count);
                        return v;
                    });
                    return;
                }
            }
        });

        if (resultMap.isEmpty()) {
            return;
        }

        System.out.println("=====" + ld + "[" + packageClass.getName() + "]=====");

        final AAReportDiffCalcu diffCalcu = new AAReportDiffCalcu(packageBoxes);
        diffCalcu.exe(resultMap.values());
        resultMap.values().stream().sorted(Comparator.comparing(it -> {
            final Line line = it.getLine();
            final Batch batch = it.getBatch();
            return batch.getBatchNo() + line.getName();
        })).forEach(System.out::println);

//        System.out.println("===============================");
    }

}