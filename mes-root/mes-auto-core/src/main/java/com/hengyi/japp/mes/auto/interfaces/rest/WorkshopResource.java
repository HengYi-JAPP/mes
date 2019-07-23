package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.WorkshopService;
import com.hengyi.japp.mes.auto.application.command.WorkshopUpdateCommand;
import com.hengyi.japp.mes.auto.application.report.WorkshopProductPlanReport;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.repository.LineMachineRepository;
import com.hengyi.japp.mes.auto.repository.LineRepository;
import com.hengyi.japp.mes.auto.repository.WorkshopRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.security.Principal;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class WorkshopResource {
    private final WorkshopService workshopService;
    private final WorkshopRepository workshopRepository;
    private final LineRepository lineRepository;
    private final LineMachineRepository lineMachineRepository;

    @Inject
    private WorkshopResource(WorkshopService workshopService, WorkshopRepository workshopRepository, LineRepository lineRepository, LineMachineRepository lineMachineRepository) {
        this.workshopService = workshopService;
        this.workshopRepository = workshopRepository;
        this.lineRepository = lineRepository;
        this.lineMachineRepository = lineMachineRepository;
    }

    @Path("workshops")
    @POST
    public Single<Workshop> create(Principal principal, WorkshopUpdateCommand command) {
        return workshopService.create(principal, command);
    }

    @Path("workshops/{id}")
    @PUT
    public Single<Workshop> update(Principal principal, @PathParam("id") @NotBlank String id, WorkshopUpdateCommand command) {
        return workshopService.update(principal, id, command);
    }

    @Path("workshops/{id}")
    @GET
    public Single<Workshop> get(@PathParam("id") @NotBlank String id) {
        return workshopRepository.find(id);
    }

    @Path("workshops")
    @GET
    public Flowable<Workshop> list() {
        return workshopRepository.list();
    }

    @Path("workshops/{id}/lines")
    @GET
    public Flowable<Line> lines(@PathParam("id") @NotBlank String id) {
        return lineRepository.listByWorkshopId(id);
    }

    @Path("workshops/{id}/productPlans")
    @GET
    public Single<WorkshopProductPlanReport> productPlans(@PathParam("id") @NotBlank String id) {
        return workshopRepository.find(id)
                .flatMapPublisher(lineRepository::listBy)
                .flatMap(lineMachineRepository::listBy).toList()
                .map(WorkshopProductPlanReport::new);
    }

    @Path("workshopsAndLines")
    @GET
    public Flowable<Map> workshopsAndLines() {
        return workshopRepository.list().flatMapSingle(workshop -> {
            final Map result = Maps.newHashMap();
            result.put("workshop", workshop);
            return lineRepository.listBy(workshop).toList().map(lines -> {
                result.put("lines", lines);
                return result;
            });
        });
    }

}
