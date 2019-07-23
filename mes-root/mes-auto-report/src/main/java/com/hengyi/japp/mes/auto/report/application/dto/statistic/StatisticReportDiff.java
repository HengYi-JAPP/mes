package com.hengyi.japp.mes.auto.report.application.dto.statistic;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxType;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.*;
import lombok.Getter;
import org.bson.Document;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static java.util.stream.Collectors.*;

/**
 * 车间 一天 唛头量 分配
 *
 * @author jzb 2019-05-29
 */
public class StatisticReportDiff {
    private final WorkshopDTO workshop;
    private final LocalDate ld;
    private final Collection<String> packageBoxIds;
    @Getter(lazy = true)
    private final Mono<StatisticReportDay> report = report();

    public StatisticReportDiff(WorkshopDTO workshop, LocalDate ld, Collection<String> packageBoxIds) {
        this.workshop = workshop;
        this.ld = ld;
        this.packageBoxIds = J.emptyIfNull(packageBoxIds);
    }

    private Mono<StatisticReportDay> report() {
        return Flux.fromIterable(packageBoxIds)
                .flatMap(this::toPackageBox)
                .collect(partitioningBy(it -> PackageBoxType.BIG_SILK_CAR == it.getType()))
                .flatMapIterable(Map::entrySet)
                .flatMapIterable(partitioningEntry -> {
                    final Boolean isBigSilkCar = partitioningEntry.getKey();
                    return partitioningEntry.getValue().parallelStream()
                            .collect(groupingBy(PackageBoxDTO::getBatch))
                            .entrySet().parallelStream()
                            .map(entry -> {
                                final BatchDTO batch = entry.getKey();
                                final List<PackageBoxDTO> packageBoxes = entry.getValue();
                                return new StatisticReportDiff_Batch(isBigSilkCar, batch, packageBoxes);
                            })
                            .collect(toList());
                })
                .collectList()
                .map(this::report);
    }

    private StatisticReportDay report(List<StatisticReportDiff_Batch> batchDiffs) {
        final StatisticReportDay report = new StatisticReportDay();
        report.setWorkshop(workshop);
        report.setLd(ld);
        report.setPackageBoxCount(packageBoxIds.size());

        final int silkCount = batchDiffs.parallelStream().collect(summingInt(StatisticReportDiff_Batch::getSilkCount));
        report.setSilkCount(silkCount);

        final BigDecimal silkWeight = batchDiffs.parallelStream().map(StatisticReportDiff_Batch::getSilkWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setSilkWeight(silkWeight);

        final List<Item> items = batchDiffs.parallelStream()
                .flatMap(it -> it.getReportItems().parallelStream())
                .collect(toList());
        report.setItems(items);

        final List<PackageBoxDTO> unDiffPackageBoxes = batchDiffs.parallelStream()
                .flatMap(it -> it.getUnDiffPackageBoxes().parallelStream())
                .collect(toList());
        report.setUnDiffPackageBoxes(unDiffPackageBoxes);

        return report;
    }

    private Mono<PackageBoxDTO> toPackageBox(String id) {
        final Document document = QueryService.find(PackageBox.class, id).block();
        final PackageBoxType type = PackageBoxType.valueOf(document.getString("type"));
//        if (Objects.equals(PackageBoxType.BIG_SILK_CAR, type)) {
//            return Mono.empty();
//        }

        final PackageBoxDTO dto = new PackageBoxDTO();
        dto.setId(document.getString(ID_COL));
        dto.setCode(document.getString("code"));
        dto.setType(type);
        dto.setBatch(BatchDTO.findFromCache(document.getString("batch")).get());
        dto.setGrade(GradeDTO.findFromCache(document.getString("grade")).get());
        dto.setSilkCount(document.getInteger("silkCount"));
        dto.setNetWeight(BigDecimal.valueOf(document.getDouble("netWeight")));
        dto.setSilkIds(document.getList("silks", String.class));
        dto.setSilkCarRecordIds(document.getList("silkCarRecords", String.class));
        dto.setSilks(fetchSilks(dto).map(it -> {
            it.setBatch(dto.getBatch());
            it.setGrade(dto.getGrade());
            return it;
        }).collectList().block());
        return Mono.just(dto);
    }

    private Flux<SilkDTO> fetchSilks(PackageBoxDTO packageBox) {
        final Collection<String> silkIds = packageBox.getSilkIds();
        if (J.nonEmpty(silkIds)) {
            return Flux.fromIterable(silkIds).flatMap(SilkDTO::fetch);
        }

        final Collection<String> silkCarRecordIds = packageBox.getSilkCarRecordIds();
        if (J.nonEmpty(silkCarRecordIds)) {
            return Flux.fromIterable(silkCarRecordIds)
                    .flatMap(it -> QueryService.find(SilkCarRecord.class, it))
                    .flatMap(document -> {
                        final Stream<String> silkIdStream = Optional.ofNullable(document.getString("initEvent"))
                                .filter(J::nonBlank)
                                .map(SilkCarRuntimeInitEvent.DTO::from)
                                .map(SilkCarRuntimeInitEvent.DTO::getSilkRuntimes)
                                .orElse(Collections.emptyList()).stream()
                                .map(SilkRuntime.DTO::getSilk)
                                .map(EntityDTO::getId);
                        return Flux.fromStream(silkIdStream);
                    })
                    .flatMap(SilkDTO::fetch);
        }
        return Flux.empty();
    }

}