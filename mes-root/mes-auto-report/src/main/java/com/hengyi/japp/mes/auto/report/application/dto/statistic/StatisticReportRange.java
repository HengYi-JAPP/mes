package com.hengyi.japp.mes.auto.report.application.dto.statistic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ixtf.japp.core.J;
import lombok.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * 统计报表 多日
 *
 * @author jzb 2019-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StatisticReportRange extends AbstractStatisticReport {
    private final LocalDate startLd;
    private final LocalDate endLd;
    @JsonIgnore
    private final Collection<StatisticReportDay> days;

    public StatisticReportRange(WorkshopDTO workshop, LocalDate startLd, LocalDate endLd, Collection<StatisticReportDay> days) {
        this.workshop = workshop;
        this.startLd = startLd;
        this.endLd = endLd;
        this.days = days;
        this.packageBoxCount = days.parallelStream().collect(summingInt(StatisticReportDay::getPackageBoxCount));
        this.silkCount = days.parallelStream().collect(summingInt(StatisticReportDay::getSilkCount));
        this.silkWeight = days.parallelStream().map(StatisticReportDay::getSilkWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.items = combine(days.parallelStream().map(StatisticReportDay::getItems));
        this.unDiffPackageBoxes = days.parallelStream().map(StatisticReportDay::getUnDiffPackageBoxes).flatMap(Collection::parallelStream).collect(toList());
        this.customDiffItems = combine(days.parallelStream().map(StatisticReportDay::getItems));
    }

    public static Collection<Item> combine(Stream<Collection<Item>> itemStream) {
        return itemStream.flatMap(Collection::parallelStream)
                .collect(groupingBy(ItemKey::new))
                .entrySet().parallelStream()
                .map(entry -> {
                    final ItemKey itemKey = entry.getKey();
                    final Item item = new Item(itemKey.isBigSilkCar(), itemKey.getLine(), itemKey.getBatch(), itemKey.getGrade());
                    final int silkCount = entry.getValue().parallelStream().collect(summingInt(Item::getSilkCount));
                    item.setSilkCount(silkCount);
                    final BigDecimal silkWeight = entry.getValue().parallelStream().map(Item::getSilkWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
                    item.setSilkWeight(silkWeight);
                    return item;
                })
                .collect(toList());
    }

    @SneakyThrows
    public StatisticReportRange testXlsx() {
        @Cleanup final Workbook wb = new XSSFWorkbook();
        PoiUtil.fillSheet1(wb.createSheet("产量"), this);
        if (J.nonEmpty(unDiffPackageBoxes)) {
            PoiUtil.fillSheet2(wb.createSheet("无法分配"), this);
        }
        @Cleanup final FileOutputStream os = new FileOutputStream("/home/jzb/" + workshop.getName() + "." + startLd + "~" + endLd + ".xlsx");
        wb.write(os);
        return this;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class ItemKey implements Serializable {
        @EqualsAndHashCode.Include
        private boolean bigSilkCar;
        @EqualsAndHashCode.Include
        private LineDTO line;
        @EqualsAndHashCode.Include
        private BatchDTO batch;
        @EqualsAndHashCode.Include
        private GradeDTO grade;

        public ItemKey(Item item) {
            this.bigSilkCar = item.isBigSilkCar();
            this.line = item.getLine();
            this.batch = item.getBatch();
            this.grade = item.getGrade();
        }
    }

}