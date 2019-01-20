package com.hengyi.japp.mes.auto.application.report;

import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jzb 2018-08-12
 */
@Data
public class StatisticsReport implements Serializable {
    private final Collection<Item> items;

    public StatisticsReport(Collection<PackageBox> packageBoxes) {
        final MeasureReport measureReport = new MeasureReport(packageBoxes);
        items = measureReport.getItems().parallelStream()
                .flatMap(this::convert)
                .collect(Collectors.toList());
    }

    private Stream<Item> convert(MeasureReport.Item item) {
        final Batch batch = item.getBatch();
        final Grade grade = item.getGrade();
        item.getPackageBoxes()
    }

    private Stream<Item> convert(MeasureReport.Item item) {
        final Batch batch = item.getBatch();
        final Grade grade = item.getGrade();
        item.getPackageBoxes()
    }

    @Data
    public static class Item implements Serializable {
        private final Line line;
        private final Batch batch;
        private final Grade grade;
        private int silkCount;
        private BigDecimal silkWeight;
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
