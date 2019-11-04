package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.DyeingService;
import com.hengyi.japp.mes.auto.application.command.DyeingResultUpdateCommand;
import com.hengyi.japp.mes.auto.application.query.DyeingPrepareQuery;
import com.hengyi.japp.mes.auto.application.query.DyeingPrepareResultQuery;
import com.hengyi.japp.mes.auto.domain.DyeingResult;
import com.hengyi.japp.mes.auto.repository.DyeingPrepareRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.validation.constraints.Min;
import javax.ws.rs.*;
import java.security.Principal;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class DyeingResource {
    private final DyeingService dyeingService;
    private final DyeingPrepareRepository dyeingPrepareRepository;

    @Inject
    private DyeingResource(DyeingService dyeingService, DyeingPrepareRepository dyeingPrepareRepository) {
        this.dyeingService = dyeingService;
        this.dyeingPrepareRepository = dyeingPrepareRepository;
    }

    @Path("dyeingPrepares/{id}/lucene")
    @PUT
    public Completable lucene(Principal principal, @PathParam("id") String id, DyeingResultUpdateCommand command) {
        return dyeingPrepareRepository.find(id).flatMap(dyeingPrepareRepository::save).ignoreElement();
//        return dyeingService.update(principal, id, command);
    }

    @Path("dyeingPrepares/{id}/result")
    @PUT
    public Completable update(Principal principal, @PathParam("id") String id, DyeingResultUpdateCommand command) {
        return dyeingService.update(principal, id, command);
    }

    @Path("dyeingPrepares/{id}/dyeingResults/{dyeingResultId}")
    @PUT
    public Completable update(Principal principal, @PathParam("id") String id, @PathParam("dyeingResultId") String dyeingResultId, DyeingResultUpdateCommand.Item command) {
        return dyeingService.update(principal, id, dyeingResultId, command);
    }

    @Path("batchDyeingPrepareResult")
    @POST
    public Completable update(Principal principal, DyeingResultUpdateCommand.Batch batch) {
        return Flowable.fromIterable(batch.getCommands()).flatMapCompletable(it -> {
            final String id = it.getDyeingPrepare().getId();
            final DyeingResultUpdateCommand command = new DyeingResultUpdateCommand();
            command.setItems(it.getItems());
            return update(principal, id, command);
        });
    }

    @Path("dyeingPrepares")
    @GET
    public Single<DyeingPrepareQuery.Result> dyeingPrepares(@QueryParam("first") @DefaultValue("0") @Min(0) int first,
                                                            @QueryParam("pageSize") @DefaultValue("50") @Min(1) int pageSize,
                                                            @QueryParam("startDateTimestamp") long startDateTimestamp,
                                                            @QueryParam("endDateTimestamp") long endDateTimestamp,
                                                            @QueryParam("type") String typeString,
                                                            @QueryParam("hrIdQ") String hrIdQ,
                                                            @QueryParam("silkCarId") String silkCarId,
                                                            @QueryParam("lineMachineId") String lineMachineId,
                                                            @QueryParam("doffingNum") String doffingNum,
                                                            @QueryParam("workshopId") String workshopId) {
        final DyeingPrepareQuery dyeingPrepareQuery = DyeingPrepareQuery.builder()
                .first(first)
                .pageSize(pageSize)
                .hrIdQ(hrIdQ)
                .silkCarId(silkCarId)
                .workshopId(workshopId)
                .lineMachineId(lineMachineId)
                .doffingNum(doffingNum)
                .startDateTimestamp(startDateTimestamp)
                .endDateTimestamp(endDateTimestamp)
                .build();
        return dyeingPrepareRepository.query(dyeingPrepareQuery);
    }

    @Path("dyeingResults")
    @GET
    public Single<DyeingPrepareResultQuery.Result> dyeingResults(@QueryParam("first") @DefaultValue("0") @Min(0) int first,
                                                                 @QueryParam("pageSize") @DefaultValue("50") @Min(1) int pageSize,
                                                                 @QueryParam("startDateTimestamp") long startDateTimestamp,
                                                                 @QueryParam("endDateTimestamp") long endDateTimestamp,
                                                                 @QueryParam("lineMachineId") String lineMachineId,
                                                                 @QueryParam("silkCarId") String silkCarId,
                                                                 @QueryParam("hrIdQ") String hrIdQ,
                                                                 @QueryParam("type") String typeString,
                                                                 @QueryParam("workshopId") String workshopId) {
        final DyeingPrepareResultQuery dyeingPrepareResultQuery = DyeingPrepareResultQuery.builder()
                .first(first)
                .pageSize(pageSize)
                .hrIdQ(hrIdQ)
                .silkCarId(silkCarId)
                .workshopId(workshopId)
                .lineMachineId(lineMachineId)
                .startDateTimestamp(startDateTimestamp)
                .endDateTimestamp(endDateTimestamp)
                .typeString(typeString)
                .build();
        return dyeingPrepareRepository.query(dyeingPrepareResultQuery);
    }

    @Path("dyeingResultsTimeline")
    @GET
    public Flowable<DyeingResult> timeline(@QueryParam("type") @DefaultValue("FIRST") String type,
                                           @QueryParam("lineMachineId") String lineMachineId,
                                           @QueryParam("spindle") @DefaultValue("1") @Min(1) int spindle,
                                           @QueryParam("size") @DefaultValue("10") @Min(1) int size,
                                           @QueryParam("currentId") String currentId) {
        return dyeingService.listTimeline(type, currentId, lineMachineId, spindle, size);
    }

}
