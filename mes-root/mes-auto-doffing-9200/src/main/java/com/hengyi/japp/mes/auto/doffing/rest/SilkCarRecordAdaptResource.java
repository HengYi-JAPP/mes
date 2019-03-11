package com.hengyi.japp.mes.auto.doffing.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.doffing.application.DoffingService;
import com.hengyi.japp.mes.auto.doffing.domain.AutoDoffingSilkCarRecordAdaptHistory;
import io.reactivex.Completable;
import io.reactivex.Single;

import javax.persistence.EntityManager;
import javax.ws.rs.*;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2019-03-09
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class SilkCarRecordAdaptResource {
    private final EntityManager em;
    private final DoffingService doffingService;

    @Inject
    private SilkCarRecordAdaptResource(EntityManager em, DoffingService doffingService) {
        this.em = em;
        this.doffingService = doffingService;
    }

    @Path("histories/{id}")
    @GET
    public Single<String> get(@PathParam("id") String id) {
        return Single.fromCallable(() -> {
            final var result = em.find(AutoDoffingSilkCarRecordAdaptHistory.class, id);
            return MAPPER.writeValueAsString(result);
        });
    }

    @Path("histories/{id}/restore")
    @PUT
    public Completable restore(@PathParam("id") String id) {
        return doffingService.restore(id);
    }
}
