package com.hengyi.japp.mes.auto.report.api;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.ReportService;
import com.hengyi.japp.mes.auto.application.command.ReportCommand;
import com.hengyi.japp.mes.auto.application.query.LocalDateRange;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQuery;
import com.hengyi.japp.mes.auto.application.query.SilkQuery;
import com.hengyi.japp.mes.auto.application.report.*;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

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

    @Inject
    private ReportServiceImpl(WorkshopRepository workshopRepository, LineRepository lineRepository, LineMachineRepository lineMachineRepository, PackageBoxRepository packageBoxRepository, SilkRepository silkRepository) {
        this.workshopRepository = workshopRepository;
        this.lineRepository = lineRepository;
        this.lineMachineRepository = lineMachineRepository;
        this.packageBoxRepository = packageBoxRepository;
        this.silkRepository = silkRepository;
    }

    @Override
    public Single<MeasureReport> measureReport(ReportCommand command) {
        final Set<@NotBlank String> budatClassIds = J.emptyIfNull(command.getPackageClasses()).stream().map(EntityDTO::getId).collect(toSet());
        final LocalDate startLd = J.localDate(command.getStartDate());
        final LocalDate endLd = J.localDate(command.getEndDate());
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .workshopId(command.getWorkshop().getId())
                .budatClassIds(budatClassIds)
                .budatRange(new LocalDateRange(startLd, endLd.plusDays(1)))
                .build();
        return packageBoxRepository.query(packageBoxQuery).map(it -> {
            final Collection<PackageBox> packageBoxes = it.getPackageBoxes();
            return new MeasureReport(packageBoxes);
        });
    }

    @Override
    public Single<MeasurePackageBoxReport> measurePackageBoxReport(ReportCommand command) {
        final LocalDate startLd = J.localDate(command.getStartDate());
        final LocalDate endLd = J.localDate(command.getEndDate());
        final Set<@NotBlank String> budatClassIds = J.emptyIfNull(command.getPackageClasses()).stream().map(EntityDTO::getId).collect(toSet());
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .workshopId(command.getWorkshop().getId())
                .budatRange(new LocalDateRange(startLd, endLd.plusDays(1)))
                .budatClassIds(budatClassIds)
                .build();
        return packageBoxRepository.query(packageBoxQuery).map(it -> {
            final Collection<PackageBox> packageBoxes = it.getPackageBoxes();
            return new MeasurePackageBoxReport(packageBoxes);
        });
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

}
