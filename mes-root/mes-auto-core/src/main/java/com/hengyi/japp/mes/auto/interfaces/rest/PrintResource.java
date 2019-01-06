package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.SilkBarcodeService;
import com.hengyi.japp.mes.auto.application.command.PrintCommand;
import com.hengyi.japp.mes.auto.domain.data.MesAutoPrinter;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;

import javax.ws.rs.GET;
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
public class PrintResource {
    private final SilkBarcodeService silkBarcodeService;
    private final SilkBarcodeRepository silkBarcodeRepository;

    @Inject
    private PrintResource(SilkBarcodeService silkBarcodeService, SilkBarcodeRepository silkBarcodeRepository) {
        this.silkBarcodeService = silkBarcodeService;
        this.silkBarcodeRepository = silkBarcodeRepository;
    }

    @Path("prints/printers")
    @GET
    public Flowable<MesAutoPrinter> get() {
        final MesAutoPrinter mesAutoPrinter = new MesAutoPrinter();
        mesAutoPrinter.setId("test");
        mesAutoPrinter.setName("test");
        return Flowable.just(mesAutoPrinter);
    }

    @Path("/prints/silkBarcodes/print")
    @POST
    public Completable print(Principal principal, PrintCommand.SilkBarcodePrintCommand command) {
        return silkBarcodeService.print(principal, command);
    }

    @Path("/prints/silks/print")
    @POST
    public Completable print(Principal principal, PrintCommand.SilkPrintCommand command) {
        return silkBarcodeService.print(principal, command);
    }
}
