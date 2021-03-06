package com.hengyi.japp.mes.auto.interfaces.rest;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.SilkBarcodeService;
import com.hengyi.japp.mes.auto.application.command.PrintCommand;
import com.hengyi.japp.mes.auto.domain.data.MesAutoPrinter;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.vertx.reactivex.redis.RedisClient;

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
    private final RedisClient redisClient;

    @Inject
    private PrintResource(SilkBarcodeService silkBarcodeService, SilkBarcodeRepository silkBarcodeRepository, RedisClient redisClient) {
        this.silkBarcodeService = silkBarcodeService;
        this.silkBarcodeRepository = silkBarcodeRepository;
        this.redisClient = redisClient;
    }

    @Path("prints/printers")
    @GET
    public Flowable<MesAutoPrinter> get() {
        return redisClient.rxPubsubChannels("SilkBarcodePrinter-*").flattenAsFlowable(it -> it).flatMapMaybe(it -> {
            final String channel = it.toString();
            final String[] split = J.split(channel, "-");
            if (split.length == 3) {
                final String id = split[1];
                final String name = split[2];
                final MesAutoPrinter mesAutoPrinter = new MesAutoPrinter();
                mesAutoPrinter.setId(id);
                mesAutoPrinter.setName(name);
                return Maybe.just(mesAutoPrinter);
            }
            return Maybe.empty();
        });
    }

    @Path("/prints/silkBarcodes/print")
    @POST
    public Completable print(Principal principal, PrintCommand.SilkBarcodePrintCommand command) {
        return Flowable.fromIterable(command.getSilkBarcodes()).map(EntityDTO::getId)
                .flatMapSingle(silkBarcodeRepository::find).toList()
                .flatMapCompletable(it -> silkBarcodeService.print(principal, command.getMesAutoPrinter(), it));
    }

    @Path("/prints/silks/print")
    @POST
    public Completable print(Principal principal, PrintCommand.SilkPrintCommand command) {
        return silkBarcodeService.print(command.getMesAutoPrinter(), command.getSilks());
    }

}
