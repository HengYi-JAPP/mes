package com.hengyi.japp.mes.auto.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.SilkCarRecordService;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.application.event.ToDtyEvent;
import com.hengyi.japp.mes.auto.application.query.SilkCarRecordQuery;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.repository.SilkCarRecordRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class SilkCarRecordResource {
    private final SilkCarRecordService silkCarRecordService;
    private final SilkCarRecordRepository silkCarRecordRepository;

    @Inject
    private SilkCarRecordResource(SilkCarRecordService silkCarRecordService, SilkCarRecordRepository silkCarRecordRepository) {
        this.silkCarRecordService = silkCarRecordService;
        this.silkCarRecordRepository = silkCarRecordRepository;
    }

    @Path("v2/ManualDoffingCheckSilks")
    @POST
    public Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeInitEvent.ManualDoffingCheckSilksCommand command) {
        return silkCarRecordService.handle(principal, command);
    }

    @Path("v2/ManualDoffingEvents")
    @POST
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.ManualDoffingCommand command) {
        return silkCarRecordService.handle(principal, command);
    }

    @Path("v3/AutoDoffingAdaptCheckSilks")
    @POST
    public Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeInitEvent.AutoDoffingOverWriteCheckSilksCommand command) {
        return silkCarRecordService.handle(principal, command);
    }

    @Path("v3/AutoDoffingAdaptEvents")
    @POST
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.AutoDoffingOverWriteCommand command) {
        return silkCarRecordService.handle(principal, command);
    }

    @Path("ToDtyEvents")
    @POST
    public Completable handle(Principal principal, ToDtyEvent.Command command) {
        return silkCarRecordService.handle(principal, command);
    }

    @Path("silkCarRecords")
    @GET
    public Single<SilkCarRecordQuery.Result> query(@QueryParam("first") @DefaultValue("0") @Min(0) int first,
                                                   @QueryParam("pageSize") @DefaultValue("50") @Min(1) int pageSize,
                                                   @QueryParam("silkCarCode") String silkCarCode,
                                                   @QueryParam("endDate") String endDate,
                                                   @QueryParam("startDate") String startDate) {
        final SilkCarRecordQuery silkCarRecordQuery = SilkCarRecordQuery.builder()
                .first(first)
                .pageSize(pageSize)
                .silkCarCode(silkCarCode)
                .startDate(LocalDate.parse(startDate))
                .endDate(LocalDate.parse(endDate))
                .build();
        return silkCarRecordRepository.query(silkCarRecordQuery);
    }

    @Path("silkCarRecords/{id}/events")
    @GET
    public Flowable<EventSource> events(@PathParam("id") @NotBlank String id) {
        return silkCarRecordRepository.find(id).flatMapPublisher(silkCarRecord -> {
            final String eventsJsonString = silkCarRecord.getEventsJsonString();
            if (J.isBlank(eventsJsonString)) {
                return Flowable.empty();
            }
            final JsonNode arrayNode = MAPPER.readTree(eventsJsonString);
            return Flowable.fromIterable(arrayNode)
                    .flatMapSingle(EventSource::from);
        });
    }

//    @Path("silkCarRecords/{id}")
//    @DELETE
//    public Completable delete(Principal principal, @PathParam("id") @NotBlank String id) {
//        return silkCarRecordRepository.find(id).flatMapCompletable(silkCarRecord -> {
//            if (silkCarRecord.getCarpoolDateTime() != null) {
//                throw new RuntimeException("拼车,无法删除");
//            }
//            return silkCarRecordRepository.delete(silkCarRecord);
//        });
//    }
}
