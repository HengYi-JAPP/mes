package com.hengyi.japp.mes.auto.doffing.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.doffing.application.LogEntityService;
import com.hengyi.japp.mes.auto.doffing.application.command.LogCreateCommand;
import io.reactivex.Completable;

import javax.persistence.EntityManager;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2019-03-09
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class LogResource {
    private final EntityManager em;
    private final LogEntityService logEntityService;

    @Inject
    private LogResource(EntityManager em, LogEntityService logEntityService) {
        this.em = em;
        this.logEntityService = logEntityService;
    }

    @Path("logs")
    @POST
    public Completable create(LogCreateCommand command) {
        return logEntityService.create(command);
    }

}
