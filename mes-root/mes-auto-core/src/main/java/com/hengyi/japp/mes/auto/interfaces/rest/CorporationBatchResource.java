package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.domain.Corporation;
import com.hengyi.japp.mes.auto.repository.CorporationRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class CorporationBatchResource {
    private final CorporationRepository corporationRepository;

    @Inject
    private CorporationBatchResource(CorporationRepository corporationRepository) {
        this.corporationRepository = corporationRepository;
    }

    @Path("corporations/{id}")
    @GET
    public Single<Corporation> get(@PathParam("id") @NotBlank String id) {
        return corporationRepository.find(id);
    }

    @Path("corporations")
    @GET
    public Flowable<Corporation> list() {
        return corporationRepository.list();
    }
}
