package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.OperatorGroupService;
import com.hengyi.japp.mes.auto.application.command.OperatorGroupUpdateCommand;
import com.hengyi.japp.mes.auto.domain.OperatorGroup;
import com.hengyi.japp.mes.auto.repository.OperatorGroupRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;

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
public class OperatorGroupResource {
    private final OperatorGroupService operatorGroupService;
    private final OperatorGroupRepository operatorGroupRepository;

    @Inject
    private OperatorGroupResource(OperatorGroupService operatorGroupService, OperatorGroupRepository operatorGroupRepository) {
        this.operatorGroupService = operatorGroupService;
        this.operatorGroupRepository = operatorGroupRepository;
    }

    @Path("operatorGroups")
    @POST
    public Single<OperatorGroup> create(Principal principal, OperatorGroupUpdateCommand command) {
        return operatorGroupService.create(principal, command);
    }

    @Path("operatorGroups/{id}")
    @PUT
    public Single<OperatorGroup> update(Principal principal, @PathParam("id") @NotBlank String id, OperatorGroupUpdateCommand command) {
        return operatorGroupService.update(principal, id, command);
    }

    @Path("operatorGroups/{id}")
    @GET
    public Single<OperatorGroup> get(@PathParam("id") @NotBlank String id) {
        return operatorGroupRepository.find(id);
    }

    @Path("operatorGroups")
    @GET
    public Flowable<OperatorGroup> list() {
        return operatorGroupRepository.list();
    }
}
