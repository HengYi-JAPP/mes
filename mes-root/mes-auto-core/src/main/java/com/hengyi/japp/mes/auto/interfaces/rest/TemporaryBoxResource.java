package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.TemporaryBoxService;
import com.hengyi.japp.mes.auto.application.command.TemporaryBoxUpdateCommand;
import com.hengyi.japp.mes.auto.domain.TemporaryBox;
import com.hengyi.japp.mes.auto.repository.TemporaryBoxRepository;
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
public class TemporaryBoxResource {
    private final TemporaryBoxService temporaryBoxService;
    private final TemporaryBoxRepository temporaryBoxRepository;

    @Inject
    private TemporaryBoxResource(TemporaryBoxService temporaryBoxService, TemporaryBoxRepository temporaryBoxRepository) {
        this.temporaryBoxService = temporaryBoxService;
        this.temporaryBoxRepository = temporaryBoxRepository;
    }

    @Path("temporaryBoxes")
    @POST
    public Single<TemporaryBox> create(Principal principal, TemporaryBoxUpdateCommand command) {
        return temporaryBoxService.create(principal, command);
    }

    @Path("temporaryBoxes/{id}")
    @PUT
    public Single<TemporaryBox> update(Principal principal, @PathParam("id") @NotBlank String id, TemporaryBoxUpdateCommand command) {
        return temporaryBoxService.update(principal, id, command);
    }

    @Path("temporaryBoxes/{id}")
    @GET
    public Single<TemporaryBox> get(@PathParam("id") @NotBlank String id) {
        return temporaryBoxRepository.find(id);
    }

    @Path("temporaryBoxes")
    @GET
    public Flowable<TemporaryBox> list() {
        return temporaryBoxRepository.list();
    }
}
