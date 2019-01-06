package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.LineMachineService;
import com.hengyi.japp.mes.auto.application.command.LineMachineUpdateCommand;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.LineMachineProductPlan;
import com.hengyi.japp.mes.auto.repository.LineMachineRepository;
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
public class LineMachineResource {
    private final LineMachineService lineMachineService;
    private final LineMachineRepository lineMachineRepository;

    @Inject
    private LineMachineResource(LineMachineService lineMachineService, LineMachineRepository lineMachineRepository) {
        this.lineMachineService = lineMachineService;
        this.lineMachineRepository = lineMachineRepository;
    }

    @Path("lineMachines")
    @POST
    public Single<LineMachine> create(Principal principal, LineMachineUpdateCommand command) {
        return lineMachineService.create(principal, command);
    }

    @Path("batchLineMachines")
    @POST
    public Completable create(Principal principal, LineMachineUpdateCommand.Batch commands) {
        return Flowable.fromIterable(commands.getCommands())
                .flatMapSingle(command -> lineMachineService.create(principal, command)).toList()
                .ignoreElement();
    }

    @Path("lineMachines/{id}")
    @PUT
    public Single<LineMachine> update(Principal principal, @PathParam("id") @NotBlank String id, LineMachineUpdateCommand command) {
        return lineMachineService.update(principal, id, command);
    }

    @Path("lineMachines/{id}")
    @GET
    public Single<LineMachine> get(@PathParam("id") @NotBlank String id) {
        return lineMachineRepository.find(id);
    }

    @Path("lineMachines/{id}/productPlan")
    @GET
    public Single<LineMachineProductPlan> productPlan(@PathParam("id") @NotBlank String id) {
        return lineMachineRepository.find(id).map(LineMachine::getProductPlan);
    }

    @Path("lineMachines/{id}/productPlansTimeline")
    @GET
    public Flowable<LineMachineProductPlan> timeline(@PathParam("id") @NotBlank String id,
                                                     @QueryParam("currentId") String currentId,
                                                     @QueryParam("size") @DefaultValue("50") @Min(1) int size) {
        return lineMachineService.listTimeline(id, currentId, size);
    }

}
