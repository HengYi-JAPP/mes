package com.hengyi.japp.mes.auto.application.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.data.SaleType;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jzb 2018-08-12
 */
@Data
public class MeasureReport implements Serializable {
    private final Collection<Item> items;

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
    }

    @Data
    public static class Item implements Serializable {
        private final Batch batch;
        private final Grade grade;
        @JsonIgnore
        private final Collection<PackageBox> packageBoxes;

        public int getSumPackageBoxCount() {
            return packageBoxes.size();
        }

        public int getDomesticPackageBoxCount() {
            return (int) packageBoxes.parallelStream()
                    .filter(it -> SaleType.DOMESTIC == it.getSaleType())
                    .count();
        }

        public int getForeignPackageBoxCount() {
            return (int) packageBoxes.parallelStream()
                    .filter(it -> SaleType.FOREIGN == it.getSaleType())
                    .count();
        }

        public int getSumSilkCount() {
            return packageBoxes.size();
        }

        public int getDomesticSilkCount() {
            return (int) packageBoxes.parallelStream()
                    .filter(it -> SaleType.DOMESTIC == it.getSaleType())
                    .mapToInt(it -> it.getSilkCount())
                    .count();
        }

        public int getForeignSilkCount() {
            return (int) packageBoxes.parallelStream()
                    .filter(it -> SaleType.FOREIGN == it.getSaleType())
                    .mapToInt(it -> it.getSilkCount())
                    .count();
        }

        public int getSumFoamCount() {
            return packageBoxes.parallelStream()
                    .mapToInt(it -> it.getFoamNum())
                    .sum();
        }

        public int getDomesticFoamCount() {
            return packageBoxes.parallelStream()
                    .filter(it -> SaleType.DOMESTIC == it.getSaleType())
                    .mapToInt(it -> it.getFoamNum())
                    .sum();
        }

        public int getForeignFoamCount() {
            return packageBoxes.parallelStream()
                    .filter(it -> SaleType.FOREIGN == it.getSaleType())
                    .mapToInt(it -> it.getFoamNum())
                    .sum();
        }
    }

    @Data
    public static class Command implements Serializable {
        @NotNull
        private final LocalDate startLd;
        @NotNull
        private final LocalDate endLd;
        @NotNull
        @Size(min = 1)
        private final Set<EntityDTO> budatClasses;
        @NotNull
        private EntityDTO workshop;
    }
}
