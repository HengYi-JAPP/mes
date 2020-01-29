package com.hengyi.japp.mes.auto.interfaces.jikon;

import com.github.ixtf.japp.vertx.annotations.ApmTrace;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterPackageBoxEvent;
import com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkCarInfoFetchEvent;
import com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkDetachEvent;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.interfaces.riamb.RiambService.PRINCIPAL;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-07
 */
@Slf4j
@Singleton
@Path("")
@Produces(APPLICATION_JSON)
public class JikonAdapterResource {
    private final JikonAdapter jikonAdapter;

    @Inject
    private JikonAdapterResource(JikonAdapter jikonAdapter) {
        this.jikonAdapter = jikonAdapter;
    }

    @ApmTrace(type = "JikonAdapterSilkCarInfoFetchEvent")
    @Path("open/automaticintegration/production/getSilkSpindleInfo")
    @POST
    public Single<String> getSilkSpindleInfo(String request) throws Exception {
        final StringBuilder sb = new StringBuilder("JikonAdapterSilkCarInfoFetchEvent").append(request);
        final JikonAdapterSilkCarInfoFetchEvent.Command command = MAPPER.readValue(request, JikonAdapterSilkCarInfoFetchEvent.Command.class);
        return jikonAdapter.handle(PRINCIPAL, command)
                .doOnSuccess(it -> log.info(sb.append("\n").append(it).toString()))
                .doOnError(ex -> log.error(sb.append("\n").append("失败!").toString(), ex));
    }

    @ApmTrace(type = "JikonAdapterSilkDetachEvent")
    @Path("open/automaticintegration/production/relieveBindSilkCar")
    @POST
    public Single<String> relieveBindSilkCar(String request) throws Exception {
        final StringBuilder sb = new StringBuilder("JikonAdapterSilkDetachEvent").append(request);
        final JikonAdapterSilkDetachEvent.Command command = MAPPER.readValue(request, JikonAdapterSilkDetachEvent.Command.class);
        return jikonAdapter.handle(PRINCIPAL, command)
                .retry(3)
                .doOnSuccess(it -> log.info(sb.append("\n").append(it).toString()))
                .doOnError(ex -> log.error(sb.append("\n").append("失败!").toString(), ex));
    }

    @ApmTrace(type = "JikonAdapterPackageBoxEvent")
    @Path("open/automaticintegration/production/getSilkBoxAndspilkInfo")
    @POST
    public Single<String> getSilkBoxAndspilkInfo(String request) throws Exception {
        final StringBuilder sb = new StringBuilder("JikonAdapterPackageBoxEvent").append(request);
        final JikonAdapterPackageBoxEvent.Command command = MAPPER.readValue(request, JikonAdapterPackageBoxEvent.Command.class);
        return jikonAdapter.handle(PRINCIPAL, command)
                .doOnSuccess(it -> log.info(sb.append("\n").append(it).toString()))
                .doOnError(ex -> log.error(sb.append("\n").append("失败!").toString(), ex));
    }

    @Path("open/okCodes/add")
    @POST
    public Completable addOkCode(EntityDTO command) throws Exception {
        return jikonAdapter.addOkCode(command);
    }

    @Path("open/okCodes/delete")
    @POST
    public Completable deleteOkCode(EntityDTO command) throws Exception {
        return jikonAdapter.deleteOkCode(command);
    }
}
