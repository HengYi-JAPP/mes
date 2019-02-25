package com.hengyi.japp.mes.auto.application.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.data.SaleType;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jzb 2018-08-12
 */
@Data
public class MeasureReport implements Serializable {
    private final Collection<Item> items;
    private final int totalPackageBoxCount;
    private final int totalDomesticPackageBoxCount;
    private final int totalForeignPackageBoxCount;

    public MeasureReport(Collection<PackageBox> packageBoxes) {
        items = J.emptyIfNull(packageBoxes).parallelStream()
                .collect(Collectors.groupingBy(it -> Pair.of(it.getBatch(), it.getGrade())))
                .entrySet().parallelStream()
                .map(entry -> {
                    final Pair<Batch, Grade> key = entry.getKey();
                    final Batch batch = key.getLeft();
                    final Grade grade = key.getRight();
                    final List<PackageBox> packageBoxList = entry.getValue();
                    return new Item(batch, grade, packageBoxList);
                })
                .collect(Collectors.toList());
        totalPackageBoxCount = J.emptyIfNull(items).parallelStream()
                .mapToInt(Item::getSumPackageBoxCount)
                .sum();
        totalDomesticPackageBoxCount = J.emptyIfNull(items).parallelStream()
                .mapToInt(Item::getDomesticPackageBoxCount)
                .sum();
        totalForeignPackageBoxCount = J.emptyIfNull(items).parallelStream()
                .mapToInt(Item::getForeignPackageBoxCount)
                .sum();
    }

    @Data
    public static class Item implements Serializable {
        private final Batch batch;
        private final Grade grade;
        @JsonIgnore
        private final Collection<PackageBox> packageBoxes;
        private final int sumPackageBoxCount;
        private final int domesticPackageBoxCount;
        private final int foreignPackageBoxCount;
        private final int sumSilkCount;
        private final int domesticSilkCount;
        private final int foreignSilkCount;
        private final BigDecimal sumNetWeight;
        private final BigDecimal domesticNetWeight;
        private final BigDecimal foreignNetWeight;
        private final int sumFoamCount;
        private final int domesticFoamCount;
        private final int foreignFoamCount;

        public Item(Batch batch, Grade grade, Collection<PackageBox> packageBoxes) {
            this.batch = batch;
            this.grade = grade;
            this.packageBoxes = J.emptyIfNull(packageBoxes);
            final Map<SaleType, List<PackageBox>> map = J.emptyIfNull(packageBoxes).parallelStream()
                    .collect(Collectors.groupingBy(PackageBox::getSaleType));
            sumPackageBoxCount = this.packageBoxes.size();
            domesticPackageBoxCount = map.getOrDefault(SaleType.DOMESTIC, Collections.EMPTY_LIST).size();
            foreignPackageBoxCount = map.getOrDefault(SaleType.FOREIGN, Collections.EMPTY_LIST).size();
            sumSilkCount = this.packageBoxes.parallelStream()
                    .mapToInt(PackageBox::getSilkCount)
                    .sum();
            domesticSilkCount = map.getOrDefault(SaleType.DOMESTIC, Collections.emptyList()).parallelStream()
                    .mapToInt(PackageBox::getSilkCount)
                    .sum();
            foreignSilkCount = map.getOrDefault(SaleType.FOREIGN, Collections.emptyList()).parallelStream()
                    .mapToInt(PackageBox::getSilkCount)
                    .sum();
            sumNetWeight = this.packageBoxes.parallelStream()
                    .map(PackageBox::getNetWeight)
                    .map(it -> {
                        final String s = Double.toString(it);
                        return new BigDecimal(s);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            domesticNetWeight = map.getOrDefault(SaleType.DOMESTIC, Collections.emptyList()).parallelStream()
                    .map(PackageBox::getNetWeight)
                    .map(it -> {
                        final String s = Double.toString(it);
                        return new BigDecimal(s);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            foreignNetWeight = map.getOrDefault(SaleType.FOREIGN, Collections.emptyList()).parallelStream()
                    .map(PackageBox::getNetWeight)
                    .map(it -> {
                        final String s = Double.toString(it);
                        return new BigDecimal(s);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            sumFoamCount = this.packageBoxes.parallelStream()
                    .mapToInt(PackageBox::getFoamNum)
                    .sum();
            domesticFoamCount = map.getOrDefault(SaleType.DOMESTIC, Collections.emptyList()).parallelStream()
                    .mapToInt(PackageBox::getFoamNum)
                    .sum();
            foreignFoamCount = map.getOrDefault(SaleType.FOREIGN, Collections.emptyList()).parallelStream()
                    .mapToInt(PackageBox::getFoamNum)
                    .sum();
        }
    }

}
