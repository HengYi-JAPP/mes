package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.AdminService;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import io.reactivex.Single;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.security.Principal;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2019-01-08
 */
@Singleton
@Path("api/admin")
@Produces(APPLICATION_JSON)
public class AdminResource {
    private final AdminService adminService;

    @Inject
    private AdminResource(AdminService adminService) {
        this.adminService = adminService;
    }

    @Path("ManualDoffingEvents")
    @POST
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.AdminManualDoffingCommand command) {
        return adminService.handle(principal, command);
    }

    @Path("lucences/PackageBoxes/{id}")
    @GET
    public Single<PackageBox> handle(Principal principal, @PathParam("id") @NotBlank String id) {
        return adminService.lucencePackageBox(principal, id);
    }
}
