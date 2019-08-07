package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.SilkCarRecordDestinationService;
import com.hengyi.japp.mes.auto.application.command.SilkCarRecordDestinationUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SilkCarRecordDestination;
import com.hengyi.japp.mes.auto.repository.SilkCarRecordDestinationRepository;
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
public class SilkCarRecordDestinationResource {
    private final SilkCarRecordDestinationService silkCarRecordDestinationService;
    private final SilkCarRecordDestinationRepository silkCarRecordDestinationRepository;

    @Inject
    private SilkCarRecordDestinationResource(SilkCarRecordDestinationService silkCarRecordDestinationService, SilkCarRecordDestinationRepository silkCarRecordDestinationRepository) {
        this.silkCarRecordDestinationService = silkCarRecordDestinationService;
        this.silkCarRecordDestinationRepository = silkCarRecordDestinationRepository;
    }

    @Path("silkCarRecordDestinations")
    @POST
    public Single<SilkCarRecordDestination> create(Principal principal, SilkCarRecordDestinationUpdateCommand command) {
        return silkCarRecordDestinationService.create(principal, command);
    }

    @Path("silkCarRecordDestinations/{id}")
    @PUT
    public Single<SilkCarRecordDestination> update(Principal principal, @PathParam("id") @NotBlank String id, SilkCarRecordDestinationUpdateCommand command) {
        return silkCarRecordDestinationService.update(principal, id, command);
    }

    @Path("silkCarRecordDestinations/{id}")
    @GET
    public Single<SilkCarRecordDestination> get(@PathParam("id") @NotBlank String id) {
        return silkCarRecordDestinationRepository.find(id);
    }

    @Path("silkCarRecordDestinations")
    @GET
    public Flowable<SilkCarRecordDestination> list() {
        return silkCarRecordDestinationRepository.list();
    }
}
