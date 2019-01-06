package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.BatchService;
import com.hengyi.japp.mes.auto.application.command.BatchUpdateCommand;
import com.hengyi.japp.mes.auto.application.query.BatchQuery;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.repository.BatchRepository;
import io.reactivex.Single;

import javax.validation.constraints.Min;
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
public class BatchResource {
    private final BatchService batchService;
    private final BatchRepository batchRepository;

    @Inject
    private BatchResource(BatchService batchService, BatchRepository batchRepository) {
        this.batchService = batchService;
        this.batchRepository = batchRepository;
    }

    @Path("batches")
    @POST
    public Single<Batch> create(Principal principal, BatchUpdateCommand command) {
        return batchService.create(principal, command);
    }

    @Path("batches/{id}")
    @PUT
    public Single<Batch> update(Principal principal, @PathParam("id") @NotBlank String id, BatchUpdateCommand command) {
        return batchService.update(principal, id, command);
    }

    @Path("batches/{id}")
    @GET
    public Single<Batch> get(@PathParam("id") @NotBlank String id) {
        return batchRepository.find(id);
    }

    @Path("batches")
    @GET
    public Single<BatchQuery.Result> query(@QueryParam("first") @DefaultValue("0") @Min(0) int first,
                                           @QueryParam("pageSize") @DefaultValue("50") @Min(1) int pageSize,
                                           @QueryParam("q") String q,
                                           @QueryParam("workshopId") String workshopId) {
        final BatchQuery batchQuery = BatchQuery.builder()
                .first(first)
                .pageSize(pageSize)
                .q(q)
                .workshopId(workshopId)
                .build();
        return batchRepository.query(batchQuery);
    }
}
