package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.PackageClassService;
import com.hengyi.japp.mes.auto.application.command.PackageClassUpdateCommand;
import com.hengyi.japp.mes.auto.domain.PackageClass;
import com.hengyi.japp.mes.auto.repository.PackageClassRepository;
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
public class PackageClassResource {
    private final PackageClassService packageClassService;
    private final PackageClassRepository packageClassRepository;

    @Inject
    private PackageClassResource(PackageClassService packageClassService, PackageClassRepository packageClassRepository) {
        this.packageClassService = packageClassService;
        this.packageClassRepository = packageClassRepository;
    }

    @Path("packageClasses")
    @POST
    public Single<PackageClass> create(Principal principal, PackageClassUpdateCommand command) {
        return packageClassService.create(principal, command);
    }

    @Path("packageClasses/{id}")
    @PUT
    public Single<PackageClass> update(Principal principal, @PathParam("id") @NotBlank String id, PackageClassUpdateCommand command) {
        return packageClassService.update(principal, id, command);
    }

    @Path("packageClasses/{id}")
    @GET
    public Single<PackageClass> get(@PathParam("id") @NotBlank String id) {
        return packageClassRepository.find(id);
    }

    @Path("packageClasses")
    @GET
    public Flowable<PackageClass> list() {
        return packageClassRepository.list();
    }
}
