package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.DictionaryService;
import com.hengyi.japp.mes.auto.application.command.DictionaryUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Dictionary;
import com.hengyi.japp.mes.auto.repository.DictionaryRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
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

    @Path("dictionaries/{id}")
    @DELETE
    public Completable delete(@PathParam("id") @NotBlank String id) {
        return dictionaryService.delete(id);
    }

    @Path("dictionaries/{key}")
    @GET
    public Flowable<Dictionary> getByKey(@PathParam("key") @NotBlank String key) {
        return dictionaryService.getByKey(key);
    }

    @Path("dictionaries")
    @GET
    public Flowable<Dictionary> get() {
        return dictionaryRepository.list();
    }
}
