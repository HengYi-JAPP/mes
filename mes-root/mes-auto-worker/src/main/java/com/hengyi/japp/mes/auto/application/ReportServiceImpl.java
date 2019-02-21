package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.query.LocalDateRange;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQuery;
import com.hengyi.japp.mes.auto.application.query.SilkQuery;
import com.hengyi.japp.mes.auto.application.report.*;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.PackageClass;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jzb 2018-08-08
 */
@Slf4j
@Singleton
public class ReportServiceImpl implements ReportService {
    private final WorkshopRepository workshopRepository;
    private final LineRepository lineRepository;
    private final LineMachineRepository lineMachineRepository;
    private final PackageBoxRepository packageBoxRepository;
    private final SilkRepository silkRepository;
    private final PackageClassRepository packageClassRepository;

    @Inject
    private ReportServiceImpl(WorkshopRepository workshopRepository, LineRepository lineRepository, LineMachineRepository lineMachineRepository, PackageBoxRepository packageBoxRepository, SilkRepository silkRepository, PackageClassRepository packageClassRepository) {
        this.workshopRepository = workshopRepository;
        this.lineRepository = lineRepository;
        this.lineMachineRepository = lineMachineRepository;
        this.packageBoxRepository = packageBoxRepository;
        this.silkRepository = silkRepository;
        this.packageClassRepository = packageClassRepository;
    }

    @Override
    public Single<MeasureReport> measureReport(String workshopId, String budatClassId, LocalDate ld) {
        final Single<Workshop> workshop$ = J.isBlank(workshopId) ? workshopRepository.list().firstOrError() : workshopRepository.find(workshopId);
        final Single<PackageClass> budatClass$ = J.isBlank(budatClassId) ? packageClassRepository.list().firstOrError() : packageClassRepository.find(budatClassId);
        return workshop$.flatMap(workshop -> budatClass$.flatMap(budatClass -> {
            final Set<String> budatClassIds = ImmutableSet.of(budatClass.getId());
            final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                    .pageSize(Integer.MAX_VALUE)
                    .workshopId(workshop.getId())
                    .budatClassIds(budatClassIds)
                    .budatRange(new LocalDateRange(ld, ld.plusDays(1)))
                    .build();
            return packageBoxRepository.query(packageBoxQuery).map(it -> {
                final Collection<PackageBox> packageBoxes = it.getPackageBoxes();
                return new MeasureReport(workshop, ld, budatClass, packageBoxes);
            });
        }));
    }

    @Override
    public Single<StatisticsReport> statisticsReport(String workshopId, LocalDate startLd, LocalDate endLd) {
        final Single<Workshop> workshop$ = J.isBlank(workshopId) ? workshopRepository.list().firstOrError() : workshopRepository.find(workshopId);
        return workshop$.flatMap(workshop -> {
            final List<LocalDate> lds = Stream.iterate(startLd, d -> d.plusDays(1))
                    .limit(ChronoUnit.DAYS.between(startLd, endLd) + 1)
                    .collect(Collectors.toList());
            return Flowable.fromIterable(lds)
                    .flatMapSingle(ld -> statisticsReportDay(workshop, ld)).toList()
                    .map(days -> new StatisticsReport(workshop, startLd, endLd, days));
//            return Flowable.fromIterable(lds)
//                    .parallel(7)
//                    .flatMap(ld -> statisticsReportDay(workshop, ld).toFlowable())
//                    .sequential()
//                    .toList()
//                    .map(days -> new StatisticsReport(workshop, startLd, endLd, days));
        });
    }

    private Single<StatisticsReportDay> statisticsReportDay(Workshop workshop, LocalDate ld) {
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .workshopId(workshop.getId())
                .budatRange(new LocalDateRange(ld, ld.plusDays(1)))
                .build();
        return packageBoxRepository.query(packageBoxQuery).map(it -> {
            final Collection<PackageBox> packageBoxes = it.getPackageBoxes();
            return new StatisticsReportDay(workshop, ld, packageBoxes);
        });
    }

    @Override
    public Single<WorkshopProductPlanReport> workshopProductPlanReport(String workshopId, String lineId) {
        final Flowable<Line> lines$;
        if (StringUtils.isNotBlank(lineId)) {
            lines$ = lineRepository.find(lineId).toFlowable();
        } else {
            final Single<Workshop> workshop$ = StringUtils.isNotBlank(workshopId)
                    ? workshopRepository.find(workshopId)
                    : workshopRepository.list().firstOrError();
            lines$ = workshop$.flatMapPublisher(lineRepository::listBy);
        }
        return lines$.flatMap(lineMachineRepository::listBy).toList()
                .map(WorkshopProductPlanReport::new);
    }

    @Override
    public Single<DoffingReport> doffingReport(String workshopId, LocalDate ldStart, LocalDate ldEnd) {
        final SilkQuery silkQuery = SilkQuery.builder()
                .workshopId(workshopId)
                .ldStart(ldStart)
                .ldEnd(ldEnd)
                .pageSize(Integer.MAX_VALUE)
                .build();
        return silkRepository.query(silkQuery).map(it -> new DoffingReport(it.getSilks()));
    }

    @Override
    public Single<PackageBoxReport> packageBoxReport(String workshopId, LocalDate ldStart, LocalDate ldEnd) {
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .workshopId(workshopId)
                .budatRange(new LocalDateRange(ldStart, ldEnd))
                .pageSize(Integer.MAX_VALUE)
                .build();
        return packageBoxRepository.query(packageBoxQuery)
                .map(it -> new PackageBoxReport(it.getPackageBoxes()));
    }

    @Override
    public Single<SilkExceptionReport> silkExceptionReport(String workshopId, LocalDate ldStart, LocalDate ldEnd) {
        final SilkQuery silkQuery = SilkQuery.builder()
                .workshopId(workshopId)
                .ldStart(ldStart)
                .ldEnd(ldEnd)
                .pageSize(Integer.MAX_VALUE)
                .build();
        return silkRepository.query(silkQuery).map(it -> new SilkExceptionReport(it.getSilks()));
    }

    @Override
    public Single<MeasurePackageBoxReport> measurePackageBoxReport(String workshopId, LocalDate startLd, LocalDate endLd, String budatClassId) {
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .workshopId(workshopId)
                .budatRange(new LocalDateRange(startLd, endLd.plusDays(1)))
                .budatClassIds(Sets.newHashSet(budatClassId))
                .build();
        return packageBoxRepository.query(packageBoxQuery).flatMap(it -> {
            final Collection<PackageBox> packageBoxes = it.getPackageBoxes();
            return packageClassRepository.find(budatClassId).map(budatClass ->
                    new MeasurePackageBoxReport(ld, budatClass, packageBoxes)
            );
        });
    }

}
