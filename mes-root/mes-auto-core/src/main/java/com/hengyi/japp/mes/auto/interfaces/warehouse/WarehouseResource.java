package com.hengyi.japp.mes.auto.interfaces.warehouse;

import com.github.ixtf.japp.vertx.annotations.ApmTrace;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.event.PackageBoxEvent;
import com.hengyi.japp.mes.auto.application.event.PackageBoxFlipEvent;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.interfaces.warehouse.event.WarehousePackageBoxFetchEvent;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;

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

    @ApmTrace(type = "WarehousePackageBoxFetchEvent")
    @Path("PackageBoxFetchEvent")
    @POST
    public Single<String> fetch(String request) {
        final StringBuilder sb = new StringBuilder("WarehousePackageBoxFetchEvent").append(request);
        return Single.fromCallable(() -> MAPPER.readValue(request, WarehousePackageBoxFetchEvent.Command.class))
                .flatMap(command -> warehouseService.handle(PRINCIPAL, command))
                .map(MAPPER::writeValueAsString)
                .doOnError(e -> log.info(sb.append("\n").append("失败").toString()))
                .doOnSuccess(it -> log.info(sb.append("\n").append(it).toString()));
    }

    @ApmTrace(type = "WarehousePackageBoxUnFetchEvent")
    @Path("packageBoxes/codes/{code}")
    @DELETE
    public Completable unFetch(@PathParam("code") String code) {
        final StringBuilder sb = new StringBuilder("PackageBoxUnFetchEvent[").append(code).append("]");
        return warehouseService.unFetch(PRINCIPAL, code)
                .doOnError(e -> log.info(sb.append("\n").append("失败").toString()))
                .doOnComplete(() -> log.info(sb.append("\n").append("成功").toString()));
    }

    @ApmTrace(type = "WarehousePackageBoxFlipEvent")
    @Path("PackageBoxFlipEvent")
    @POST
    public Completable packageBoxFlipEvent(String request) {
        final StringBuilder sb = new StringBuilder("WarehousePackageBoxFlipEvent").append(request);
        return Single.fromCallable(() -> MAPPER.readValue(request, PackageBoxFlipEvent.WarehouseCommand.class))
                .flatMap(command -> warehouseService.handle(PRINCIPAL, command)).ignoreElement()
                .doOnError(e -> log.info(sb.append("\n").append("失败").toString()))
                .doOnComplete(() -> log.info(sb.append("\n").append("成功").toString()));
    }

    @Path("measureInfo")
    @POST
    public Single<PackageBox> handle(WarehouseMeasureInfoUpdateCommand command) {
        return warehouseService.handle(PRINCIPAL, command);
    }

    @Path("bigSilkCarPackageBoxes")
    @POST
    public Single<PackageBox> handle(PackageBoxEvent.BigSilkCarCommand command) {
        return warehouseService.handle(PRINCIPAL, command);
    }
}
