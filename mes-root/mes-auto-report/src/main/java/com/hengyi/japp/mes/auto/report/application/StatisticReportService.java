package com.hengyi.japp.mes.auto.report.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQuery;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.exception.DailyReportNotExistException;
import com.hengyi.japp.mes.auto.interfaces.search.SearchService;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.AbstractStatisticReport.WorkshopDTO;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.StatisticReportDay;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.StatisticReportDiff;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.StatisticReportRange;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-05-20
 */
@Slf4j
@Singleton
public class StatisticReportService {
    private final File baseDir;
    private final SearchService searchService;

    @Inject
    private StatisticReportService(MesAutoConfig mesAutoConfig, SearchService searchService) {
        this.searchService = searchService;
        baseDir = mesAutoConfig.reportPath("PackageBoxStatisticReport").toFile();
    }

    private File file(WorkshopDTO workshop, LocalDate ld) {
        final String ldString = ld.toString();
        final File dir = FileUtils.getFile(baseDir, ldString.split("-"));
        final String fileName = String.join(".", workshop.getCode(), ldString, "yml");
        return FileUtils.getFile(dir, fileName);
    }

    public Mono<StatisticReportDay> generate(String workshopId, LocalDate ld) {
        final Collection<String> packageBoxIds = packageBoxIds(workshopId, ld);
        final WorkshopDTO workshop = WorkshopDTO.findFromCache(workshopId).get();
        return new StatisticReportDiff(workshop, ld, packageBoxIds).getReport().map(report -> {
            final File file = file(report.getWorkshop(), report.getLd());
            return report.saveYaml(file);
        });
    }

    @SneakyThrows
    public Collection<String> packageBoxIds(final String workshopId, final LocalDate ld) {
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .inWarehouse(true)
                .workshopId(workshopId)
                .startBudat(ld)
                .endBudat(ld.plusDays(1))
                .build();
        final Pair<Long, Collection<String>> pair = searchService.query(packageBoxQuery);
        return pair.getRight();
    }

    public Mono<StatisticReportDay> fromDisk(String workshopId, LocalDate ld) {
        final WorkshopDTO workshop = WorkshopDTO.findFromCache(workshopId).get();
        return Mono.justOrEmpty(StatisticReportDay.from(file(workshop, ld)))
                .onErrorResume(DailyReportNotExistException.class, err -> generate(workshopId, ld));
    }

    public StatisticReportRange rangeDisk(String workshopId, LocalDate startLd, LocalDate endLd) {
        final WorkshopDTO workshop = WorkshopDTO.findFromCache(workshopId).get();
        final List<StatisticReportDay> reports = Stream.iterate(startLd, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startLd, endLd) + 1).parallel()
                .map(ld -> {
                    final File file = file(workshop, ld);
                    return StatisticReportDay.from(file);
                })
                .collect(toList());
        return new StatisticReportRange(workshop, startLd, endLd, reports);
    }
}
