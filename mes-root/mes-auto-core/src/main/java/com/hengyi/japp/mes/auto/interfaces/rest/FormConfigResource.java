package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.FormConfigService;
import com.hengyi.japp.mes.auto.application.command.FormConfigUpdateCommand;
import com.hengyi.japp.mes.auto.application.query.FormConfigQuery;
import com.hengyi.japp.mes.auto.domain.FormConfig;
import com.hengyi.japp.mes.auto.repository.FormConfigRepository;
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
public class FormConfigResource {
    private final FormConfigService formConfigService;
    private final FormConfigRepository formConfigRepository;

    @Inject
    private FormConfigResource(FormConfigService formConfigService, FormConfigRepository formConfigRepository) {
        this.formConfigService = formConfigService;
        this.formConfigRepository = formConfigRepository;
    }

    @Path("formConfigs")
    @POST
    public Single<FormConfig> create(Principal principal, FormConfigUpdateCommand command) {
        return formConfigService.create(principal, command);
    }

    @Path("formConfigs/{id}")
    @PUT
    public Single<FormConfig> update(Principal principal, @PathParam("id") @NotBlank String id, FormConfigUpdateCommand command) {
        return formConfigService.update(principal, id, command);
    }

    @Path("formConfigs/{id}")
    @GET
    public Single<FormConfig> get(@PathParam("id") @NotBlank String id) {
        return formConfigRepository.find(id);
    }

    @Path("formConfigs")
    @GET
    public Single<FormConfigQuery.Result> get(@QueryParam("first") @DefaultValue("0") @Min(0) int first,
                                              @QueryParam("pageSize") @DefaultValue("50") @Min(1) int pageSize,
                                              @QueryParam("q") String q) {
        final FormConfigQuery formConfigQuery = FormConfigQuery.builder()
                .first(first)
                .pageSize(pageSize)
                .q(q)
                .build();
        return formConfigRepository.query(formConfigQuery);
    }
}
