package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.DictionaryService;
import com.hengyi.japp.mes.auto.application.command.DictionaryUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Dictionary;
import com.hengyi.japp.mes.auto.repository.DictionaryRepository;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.security.Principal;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author liuyuan
 * @create 2019-03-14 14:01
 * @description
 **/
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class DictionaryResource {
    private final DictionaryService dictionaryService;
    private final DictionaryRepository dictionaryRepository;

    @Inject
    private DictionaryResource(DictionaryService dictionaryService, DictionaryRepository dictionaryRepository) {
        this.dictionaryService = dictionaryService;
        this.dictionaryRepository = dictionaryRepository;
    }

    @Path("dictionaries")
    @POST
    public Single<Dictionary> create(Principal principal, DictionaryUpdateCommand command) {
        return dictionaryService.create(principal, command);
    }

    @Path("dictionaries/{id}")
    @PUT
    public Single<Dictionary> update(Principal principal, @PathParam("id") @NotBlank String id, DictionaryUpdateCommand command) {
        return dictionaryService.update(principal, id, command);
    }

    //    @Path("dictionaries")
//    @GET
//    public Single<DictionaryQuery.Result> query(@QueryParam("first") @DefaultValue("0") @Min(0) int first,
//                                                @QueryParam("pageSize") @DefaultValue("50") @Min(1) int pageSize){
//        final DictionaryQuery dictionaryQuery = DictionaryQuery.builder()
//                .first(first)
//                .pageSize(pageSize)
//                .build();
//        return dictionaryRepository.query(dictionaryQuery);
//    }
    @Path("dictionaries/{key}")
    @GET
    public Maybe<Dictionary> getByKey(@PathParam("key") @NotBlank String key) {
        return dictionaryService.getByKey(key);
    }

    @Path("dictionaries")
    @GET
    public Flowable<Dictionary> get() {
        return dictionaryRepository.list();
    }
}
