package com.hengyi.japp.mes.auto.interfaces.rest;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.OperatorService;
import com.hengyi.japp.mes.auto.application.command.OperatorCreateCommand;
import com.hengyi.japp.mes.auto.application.command.OperatorPermissionUpdateCommand;
import com.hengyi.japp.mes.auto.application.command.OperatorUpdateCommand;
import com.hengyi.japp.mes.auto.application.command.PasswordChangeCommand;
import com.hengyi.japp.mes.auto.application.query.OperatorQuery;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import io.reactivex.Completable;
import io.reactivex.Single;

import javax.validation.constraints.Min;
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
public class OperatorResource {
    private final OperatorService operatorService;
    private final OperatorRepository operatorRepository;

    @Inject
    private OperatorResource(OperatorService operatorService, OperatorRepository operatorRepository) {
        this.operatorService = operatorService;
        this.operatorRepository = operatorRepository;
    }

//    @Path("operators")
//    @POST
//    public Single<Operator> create(Principal principal, OperatorImportCommand command) {
//        return operatorService.create(principal, command);
//    }

    @Path("operators")
    @POST
    public Single<Operator> create(Principal principal, OperatorCreateCommand command) {
        return operatorService.create(principal, command);
    }

    @Path("operators/{id}")
    @PUT
    public Single<Operator> update(Principal principal, @PathParam("id") @NotBlank String id, OperatorPermissionUpdateCommand command) {
        return operatorService.update(principal, id, command);
    }

    @Path("operators/{id}/info")
    @PUT
    public Single<Operator> updateInfo(Principal principal, @PathParam("id") @NotBlank String id, OperatorUpdateCommand command) {
        return operatorService.updateInfo(principal, id, command);
    }

    @Path("operators/{id}/password")
    @PUT
    public Completable password(@PathParam("id") @NotBlank String id, PasswordChangeCommand command) {
        return operatorService.password(id, command);
    }

    @Path("operators/{id}")
    @GET
    public Single<Operator> get(@PathParam("id") @NotBlank String id) {
        return operatorRepository.find(id);
    }


    @Path("operators/{id}/auth")
    @GET
    public Single<Map> auth(@PathParam("id") @NotBlank String id) {
        return operatorRepository.find(id).map(operator -> ImmutableMap.of(
                "admin", operator.isAdmin(),
                "groups", J.emptyIfNull(operator.getGroups()),
                "roles", J.emptyIfNull(operator.getRoles()),
                "permissions", J.emptyIfNull(operator.getPermissions())
        ));
    }

    @Path("operators")
    @GET
    public Single<OperatorQuery.Result> query(@QueryParam("first") @DefaultValue("0") @Min(0) int first,
                                              @QueryParam("pageSize") @DefaultValue("50") @Min(1) int pageSize,
                                              @QueryParam("q") String q) {
        final OperatorQuery operatorQuery = OperatorQuery.builder()
                .first(first)
                .pageSize(pageSize)
                .q(q)
                .build();
        return operatorRepository.query(operatorQuery);
    }
}
