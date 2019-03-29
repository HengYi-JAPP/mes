package com.hengyi.japp.mes.auto.interfaces.jikon;

import com.github.ixtf.japp.vertx.annotations.ApmTrace;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterPackageBoxEvent;
import com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkCarInfoFetchEvent;
import com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkDetachEvent;
import io.reactivex.Single;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.interfaces.jikon.JikonAdapter.LOG;
import static com.hengyi.japp.mes.auto.interfaces.riamb.RiambService.PRINCIPAL;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-07
 */
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
        final JikonAdapterSilkCarInfoFetchEvent.Command command = MAPPER.readValue(request, JikonAdapterSilkCarInfoFetchEvent.Command.class);
        return jikonAdapter.handle(PRINCIPAL, command).doOnSuccess(it -> {
            final StringBuilder sb = new StringBuilder("JikonAdapterSilkCarInfoFetchEvent").append(request)
                    .append("\n").append(it);
            LOG.info(sb.toString());
        });
    }

    @ApmTrace(type = "JikonAdapterSilkDetachEvent")
    @Path("open/automaticintegration/production/relieveBindSilkCar")
    @POST
    public Single<String> relieveBindSilkCar(String request) throws Exception {
        final JikonAdapterSilkDetachEvent.Command command = MAPPER.readValue(request, JikonAdapterSilkDetachEvent.Command.class);
        return jikonAdapter.handle(PRINCIPAL, command).doOnSuccess(it -> {
            final StringBuilder sb = new StringBuilder("JikonAdapterSilkDetachEvent").append(request)
                    .append("\n").append(it);
            LOG.info(sb.toString());
        });
    }

    @ApmTrace(type = "JikonAdapterPackageBoxEvent")
    @Path("open/automaticintegration/production/getSilkBoxAndspilkInfo")
    @POST
    public Single<String> getSilkBoxAndspilkInfo(String request) throws Exception {
        final JikonAdapterPackageBoxEvent.Command command = MAPPER.readValue(request, JikonAdapterPackageBoxEvent.Command.class);
        return jikonAdapter.handle(PRINCIPAL, command).doOnSuccess(it -> {
            final StringBuilder sb = new StringBuilder("JikonAdapterPackageBoxEvent").append(request)
                    .append("\n").append(it);
            LOG.info(sb.toString());
        });
    }
}
