package com.hengyi.japp.mes.auto.application;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Single<MeasureReport> measureReport(MeasureReport.Command command) {
        final Set<String> budatClassIds = command.getBudatClasses().stream()
                .map(EntityDTO::getId)
                .collect(Collectors.toSet());
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .workshopId(command.getWorkshop().getId())
                .budatClassIds(budatClassIds)
                .budatRange(new LocalDateRange(command.getStartLd(), command.getEndLd()))
                .build();
        return packageBoxRepository.query(packageBoxQuery).map(it -> {
            final Collection<PackageBox> packageBoxes = it.getPackageBoxes();
            return new MeasureReport(packageBoxes);
        });
    }

    @Override
    public Single<StatisticsReport> statisticsReport(StatisticsReport.Command command) {
        final Set<String> budatClassIds = command.getBudatClasses().stream()
                .map(EntityDTO::getId)
                .collect(Collectors.toSet());
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .workshopId(command.getWorkshop().getId())
                .budatClassIds(budatClassIds)
                .budatRange(new LocalDateRange(command.getStartLd(), command.getEndLd()))
                .build();
        return packageBoxRepository.query(packageBoxQuery).map(it -> {
            final Collection<PackageBox> packageBoxes = it.getPackageBoxes();
            return new StatisticsReport(packageBoxes);
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
    public Single<MeasurePackageBoxReport> measurePackageBoxReport(LocalDate ld, String budatClassId) {
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .budatRange(new LocalDateRange(ld, ld.plusDays(1)))
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
