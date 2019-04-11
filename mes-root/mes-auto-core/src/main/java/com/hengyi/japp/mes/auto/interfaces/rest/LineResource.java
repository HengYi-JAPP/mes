package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.LineService;
import com.hengyi.japp.mes.auto.application.command.LineUpdateCommand;
import com.hengyi.japp.mes.auto.application.query.LineQuery;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.SilkBarcodeGenerateTemplate;
import com.hengyi.japp.mes.auto.repository.LineMachineRepository;
import com.hengyi.japp.mes.auto.repository.LineRepository;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeGenerateTemplateRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.security.Principal;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class LineResource {
    private final LineService lineService;
    private final LineRepository lineRepository;
    private final LineMachineRepository lineMachineRepository;
    private final SilkBarcodeGenerateTemplateRepository silkBarcodeGenerateTemplateRepository;

    @Inject
    private LineResource(LineService lineService, LineRepository lineRepository, LineMachineRepository lineMachineRepository, SilkBarcodeGenerateTemplateRepository silkBarcodeGenerateTemplateRepository) {
        this.lineService = lineService;
        this.lineRepository = lineRepository;
        this.lineMachineRepository = lineMachineRepository;
        this.silkBarcodeGenerateTemplateRepository = silkBarcodeGenerateTemplateRepository;
    }

    @Path("lines")
    @POST
    public Single<Line> create(Principal principal, LineUpdateCommand command) {
        return lineService.create(principal, command);
    }

    @Path("batchLines")
    @POST
    public Completable create(Principal principal, LineUpdateCommand.Batch commands) {
        return Flowable.fromIterable(commands.getCommands())
                .flatMapSingle(command -> lineService.create(principal, command))
                .toList()
                .ignoreElement();
    }

    @Path("lines/{id}")
    @PUT
    public Single<Line> update(Principal principal, @PathParam("id") @NotBlank String id, LineUpdateCommand command) {
        return lineService.update(principal, id, command);
    }

    @Path("lines/{id}")
    @GET
    public Single<Line> get(@PathParam("id") @NotBlank String id) {
        return lineRepository.find(id);
    }

    @Path("lines/{id}/lineMachines")
    @GET
    public Flowable<LineMachine> lineMachines(@PathParam("id") @NotBlank String id) {
        return lineMachineRepository.listByLineId(id);
    }

    @Path("lines/{id}/silkBarcodeGenerateTemplates")
    @GET
    public Flowable<SilkBarcodeGenerateTemplate> silkBarcodeGenerateTemplates(@PathParam("id") @NotBlank String id) {
        return silkBarcodeGenerateTemplateRepository.listByLineId(id);
    }

    @Path("lines")
    @GET
    public Single<LineQuery.Result> query(@QueryParam("first") @DefaultValue("0") @Min(0) int first,
                                          @QueryParam("pageSize") @DefaultValue("50") @Min(1) int pageSize,
                                          @QueryParam("q") String q,
                                          @QueryParam("workshopId") String workshopId) {
        final LineQuery lineQuery = LineQuery.builder()
                .first(first)
                .pageSize(pageSize)
                .q(q)
                .workshopId(workshopId)
                .build();
        return lineRepository.query(lineQuery);
    }
}
