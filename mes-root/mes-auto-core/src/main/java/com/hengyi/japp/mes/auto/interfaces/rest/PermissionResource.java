package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.PermissionService;
import com.hengyi.japp.mes.auto.application.command.PermissionUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Permission;
import com.hengyi.japp.mes.auto.domain.data.RoleType;
import com.hengyi.japp.mes.auto.repository.PermissionRepository;
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
public class PermissionResource {
    private final PermissionService permissionService;
    private final PermissionRepository permissionRepository;

    @Inject
    private PermissionResource(PermissionService permissionService, PermissionRepository permissionRepository) {
        this.permissionService = permissionService;
        this.permissionRepository = permissionRepository;
    }

    @Path("permissions")
    @POST
    public Single<Permission> create(Principal principal, PermissionUpdateCommand command) {
        return permissionService.create(principal, command);
    }

    @Path("permissions/{id}")
    @PUT
    public Single<Permission> update(Principal principal, @PathParam("id") @NotBlank String id, PermissionUpdateCommand command) {
        return permissionService.update(principal, id, command);
    }

    @Path("permissions/{id}")
    @GET
    public Single<Permission> get(@PathParam("id") @NotBlank String id) {
        return permissionRepository.find(id);
    }

    @Path("permissions")
    @GET
    public Flowable<Permission> get() {
        return permissionRepository.list();
    }

    @Path("roles")
    @GET
    public RoleType[] roles() {
        return RoleType.values();
    }
}
