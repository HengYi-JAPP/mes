package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.AdminService;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import io.reactivex.Completable;
import io.reactivex.Single;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

    @Path("SilkBarcodeRepositoryMongo/lock")
    @DELETE
    public Completable handle(Principal principal) {
        return adminService.unlockSilkBarcodeRepositoryMongo(principal);
    }

}
