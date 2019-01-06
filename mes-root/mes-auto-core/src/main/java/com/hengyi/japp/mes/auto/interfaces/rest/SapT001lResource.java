package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.SapT001lService;
import com.hengyi.japp.mes.auto.application.command.SapT001lUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SapT001l;
import com.hengyi.japp.mes.auto.repository.SapT001lRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-11
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class SapT001lResource {
    private final SapT001lService sapT001lService;
    private final SapT001lRepository sapT001lRepository;

    @Inject
    private SapT001lResource(SapT001lService sapT001lService, SapT001lRepository sapT001lRepository) {
        this.sapT001lService = sapT001lService;
        this.sapT001lRepository = sapT001lRepository;
    }

    @Path("sapT001ls")
    @POST
    public Single<SapT001l> create(SapT001lUpdateCommand command) {
        return sapT001lService.create(command);
    }

    @Path("sapT001ls/{lgort}")
    @GET
    public Single<SapT001l> get(@PathParam("lgort") String lgort) {
        return sapT001lRepository.find(lgort);
    }

    @Path("sapT001ls")
    @GET
    public Flowable<SapT001l> list() {
        return sapT001lRepository.list();
    }
}
