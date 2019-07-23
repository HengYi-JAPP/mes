package com.hengyi.japp.mes.auto.interfaces.rest;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.ReportCommand;
import com.hengyi.japp.mes.auto.application.query.LocalDateRange;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQuery;
import com.hengyi.japp.mes.auto.application.report.MeasurePackageBoxReport;
import com.hengyi.japp.mes.auto.application.report.WorkshopProductPlanReport;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.LineMachineRepository;
import com.hengyi.japp.mes.auto.repository.LineRepository;
import com.hengyi.japp.mes.auto.repository.PackageBoxRepository;
import com.hengyi.japp.mes.auto.repository.WorkshopRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api/reports")
@Produces(APPLICATION_JSON)
public class ReportResource {
    private final WorkshopRepository workshopRepository;
    private final LineRepository lineRepository;
    private final LineMachineRepository lineMachineRepository;
    private final PackageBoxRepository packageBoxRepository;

    @Inject
    public ReportResource(WorkshopRepository workshopRepository, LineRepository lineRepository, LineMachineRepository lineMachineRepository, PackageBoxRepository packageBoxRepository) {
        this.workshopRepository = workshopRepository;
        this.lineRepository = lineRepository;
        this.lineMachineRepository = lineMachineRepository;
        this.packageBoxRepository = packageBoxRepository;
    }

    @Path("workshopProductPlanReport")
    @GET
    public Single<WorkshopProductPlanReport> workshopProductPlanReport(@QueryParam("workshopId") String workshopId,
                                                                       @QueryParam("lineId") String lineId) {
        final Flowable<Line> lines$;
        if (J.nonBlank(lineId)) {
            lines$ = lineRepository.find(lineId).toFlowable();
        } else {
            final Single<Workshop> workshop$ = J.nonBlank(workshopId)
                    ? workshopRepository.find(workshopId)
                    : workshopRepository.list().firstOrError();
            lines$ = workshop$.flatMapPublisher(lineRepository::listBy);
        }
        return lines$.flatMap(lineMachineRepository::listBy).toList()
                .map(WorkshopProductPlanReport::new);
    }

    @Path("measurePackageBoxReport")
    @POST
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

}
