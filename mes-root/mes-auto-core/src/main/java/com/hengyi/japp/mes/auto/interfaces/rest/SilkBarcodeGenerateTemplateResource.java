package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.SilkBarcodeGenerateTemplateService;
import com.hengyi.japp.mes.auto.application.command.SilkBarcodeGenerateTemplateUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SilkBarcodeGenerateTemplate;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeGenerateTemplateRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.security.Principal;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class SilkBarcodeGenerateTemplateResource {
    private final SilkBarcodeGenerateTemplateService silkBarcodeGenerateTemplateService;
    private final SilkBarcodeGenerateTemplateRepository silkBarcodeGenerateTemplateRepository;

    @Inject
    private SilkBarcodeGenerateTemplateResource(SilkBarcodeGenerateTemplateService silkBarcodeGenerateTemplateService, SilkBarcodeGenerateTemplateRepository silkBarcodeGenerateTemplateRepository) {
        this.silkBarcodeGenerateTemplateService = silkBarcodeGenerateTemplateService;
        this.silkBarcodeGenerateTemplateRepository = silkBarcodeGenerateTemplateRepository;
    }

    @Path("silkBarcodeGenerateTemplates")
    @POST
    public Single<SilkBarcodeGenerateTemplate> create(Principal principal, SilkBarcodeGenerateTemplateUpdateCommand command) {
        return silkBarcodeGenerateTemplateService.create(principal, command);
    }

    @Path("batchSilkBarcodeGenerateTemplates")
    @POST
    public Flowable<SilkBarcodeGenerateTemplate> create(Principal principal, SilkBarcodeGenerateTemplateUpdateCommand.Batch commands) {
        return Flowable.fromIterable(commands.getCommands()).flatMapSingle(command -> create(principal, command));
    }
}
