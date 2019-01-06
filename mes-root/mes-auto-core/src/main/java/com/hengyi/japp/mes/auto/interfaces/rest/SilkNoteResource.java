package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.SilkNoteService;
import com.hengyi.japp.mes.auto.application.command.SilkNoteUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SilkNote;
import com.hengyi.japp.mes.auto.repository.SilkNoteRepository;
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
public class SilkNoteResource {
    private final SilkNoteService silkNoteService;
    private final SilkNoteRepository silkNoteRepository;

    @Inject
    private SilkNoteResource(SilkNoteService silkNoteService, SilkNoteRepository silkNoteRepository) {
        this.silkNoteService = silkNoteService;
        this.silkNoteRepository = silkNoteRepository;
    }

    @Path("silkNotes")
    @POST
    public Single<SilkNote> create(Principal principal, SilkNoteUpdateCommand command) {
        return silkNoteService.create(principal, command);
    }

    @Path("silkNotes/{id}")
    @PUT
    public Single<SilkNote> update(Principal principal, @PathParam("id") @NotBlank String id, SilkNoteUpdateCommand command) {
        return silkNoteService.update(principal, id, command);
    }

    @Path("silkNotes/{id}")
    @GET
    public Single<SilkNote> get(@PathParam("id") @NotBlank String id) {
        return silkNoteRepository.find(id);
    }

    @Path("silkNotes")
    @GET
    public Flowable<SilkNote> list() {
        return silkNoteRepository.list();
    }
}
