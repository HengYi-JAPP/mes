package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.GradeService;
import com.hengyi.japp.mes.auto.application.command.GradeUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.repository.GradeRepository;
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
public class GradeResource {
    private final GradeService gradeService;
    private final GradeRepository gradeRepository;

    @Inject
    private GradeResource(GradeService gradeService, GradeRepository gradeRepository) {
        this.gradeService = gradeService;
        this.gradeRepository = gradeRepository;
    }

    @Path("grades")
    @POST
    public Single<Grade> create(Principal principal, GradeUpdateCommand command) {
        return gradeService.create(principal, command);
    }

    @Path("grades/{id}")
    @PUT
    public Single<Grade> update(Principal principal, @PathParam("id") @NotBlank String id, GradeUpdateCommand command) {
        return gradeService.update(principal, id, command);
    }

    @Path("grades/{id}")
    @GET
    public Single<Grade> get(@PathParam("id") @NotBlank String id) {
        return gradeRepository.find(id);
    }

    @Path("grades")
    @GET
    public Flowable<Grade> get() {
        return gradeRepository.list();
    }
}
