package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.AdminService;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeRepository;
import io.reactivex.Completable;
import io.reactivex.Single;

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
    private final SilkBarcodeRepository silkBarcodeRepository;

    @Inject
    private AdminResource(AdminService adminService, SilkBarcodeRepository silkBarcodeRepository) {
        this.adminService = adminService;
        this.silkBarcodeRepository = silkBarcodeRepository;
    }

    @Path("ManualDoffingEvents")
    @POST
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.AdminManualDoffingCommand command) {
        return adminService.handle(principal, command);
    }

    @Path("AutoDoffingAdaptEvents")
    @POST
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.AdminAutoDoffingAdaptCommand command) {
        return adminService.handle(principal, command);
    }

    @Path("silkBarcodes/{id}/lucence")
    @PUT
    public Completable handle(Principal principal, @PathParam("id") String id) {
        return silkBarcodeRepository.find(id).flatMap(silkBarcode -> {
            final String code = silkBarcode.getCode();
            silkBarcode.setCode(code);
            return silkBarcodeRepository.save(silkBarcode);
        }).ignoreElement();
    }

}
