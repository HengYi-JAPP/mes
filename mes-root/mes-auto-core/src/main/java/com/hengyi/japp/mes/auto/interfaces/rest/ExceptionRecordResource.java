package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.ExceptionRecordService;
import com.hengyi.japp.mes.auto.application.command.ExceptionRecordUpdateCommand;
import com.hengyi.japp.mes.auto.application.query.ExceptionRecordQuery;
import com.hengyi.japp.mes.auto.domain.ExceptionRecord;
import com.hengyi.japp.mes.auto.repository.ExceptionRecordRepository;
import io.reactivex.Completable;
import io.reactivex.Single;

import javax.ws.rs.*;
import java.security.Principal;
import java.util.Collection;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class ExceptionRecordResource {
    private final ExceptionRecordService exceptionRecordService;
    private final ExceptionRecordRepository exceptionRecordRepository;

    @Inject
    private ExceptionRecordResource(ExceptionRecordService exceptionRecordService, ExceptionRecordRepository exceptionRecordRepository) {
        this.exceptionRecordService = exceptionRecordService;
        this.exceptionRecordRepository = exceptionRecordRepository;
    }

    @Path("exceptionRecords")
    @POST
    public Single<ExceptionRecord> create(Principal principal, ExceptionRecordUpdateCommand command) {
        return exceptionRecordService.create(principal, command);
    }

    @Path("exceptionRecords/{id}")
    @PUT
    public Single<ExceptionRecord> update(Principal principal, @PathParam("id") String id, ExceptionRecordUpdateCommand command) {
        return exceptionRecordService.update(principal, id, command);
    }

    @Path("exceptionRecords/{id}/handle")
    @PUT
    public Completable exceptionRecords(Principal principal, @PathParam("id") String id) {
        return exceptionRecordService.handle(principal, id);
    }

    @Path("exceptionRecords")
    @GET
    public Single<Collection<ExceptionRecord>> exceptionRecords() {
        final ExceptionRecordQuery query = ExceptionRecordQuery.builder().build();
        return exceptionRecordRepository.query(query).map(ExceptionRecordQuery.Result::getResult);
    }

}
