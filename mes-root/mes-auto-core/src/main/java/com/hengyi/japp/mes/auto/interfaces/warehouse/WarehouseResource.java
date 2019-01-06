package com.hengyi.japp.mes.auto.interfaces.warehouse;

import com.github.ixtf.japp.vertx.annotations.ApmTrace;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.event.PackageBoxFlipEvent;
import com.hengyi.japp.mes.auto.interfaces.warehouse.event.WarehousePackageBoxFetchEvent;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.interfaces.warehouse.WarehouseService.PRINCIPAL;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * 仓库接口
 *
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
@Path("warehouse")
@Produces(APPLICATION_JSON)
public class WarehouseResource {
    private final WarehouseService warehouseService;

    @Inject
    private WarehouseResource(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @SneakyThrows
    @ApmTrace(type = "WarehousePackageBoxFetchEvent")
    @Path("PackageBoxFetchEvent")
    @POST
    public Single<String> fetch(String request) {
        final StringBuilder sb = new StringBuilder("WarehousePackageBoxFetchEvent").append(request);
        log.info(sb.toString());
        final WarehousePackageBoxFetchEvent.Command command = MAPPER.readValue(request, WarehousePackageBoxFetchEvent.Command.class);
        return warehouseService.handle(PRINCIPAL, command).map(MAPPER::writeValueAsString)
                .doOnSuccess(it -> log.info(sb.append("\n").append(it).toString()));
    }

    @SneakyThrows
    @ApmTrace(type = "WarehousePackageBoxFlipEvent")
    @Path("PackageBoxFlipEvent")
    @POST
    public Completable packageBoxFlipEvent(String request) {
        final StringBuilder sb = new StringBuilder("WarehousePackageBoxFlipEvent").append(request);
        log.info(sb.toString());
        final PackageBoxFlipEvent.WarehouseCommand command = MAPPER.readValue(request, PackageBoxFlipEvent.WarehouseCommand.class);
        return warehouseService.handle(PRINCIPAL, command).ignoreElement()
                .doOnComplete(() -> log.info(sb.append("\n").append("OK").toString()));
    }
}
