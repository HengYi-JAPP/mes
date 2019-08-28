package com.hengyi.japp.mes.auto.agent.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.Util;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import io.reactivex.Completable;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import static com.hengyi.japp.mes.auto.agent.Agent.INJECTOR;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-06-20
 */
@Slf4j
public class PdaVerticle extends AbstractVerticle {

    @Override
    public Completable rxStart() {
        final MesAutoConfig config = INJECTOR.getInstance(MesAutoConfig.class);

        final Router router = Router.router(vertx);
        Jvertx.enableCommon(router);
        Jvertx.enableCors(router, config.getCorsConfig().getDomainPatterns());
        router.route().failureHandler(Jvertx::failureHandler);

        router.get("/apkInfo").produces(APPLICATION_JSON).handler(rc -> rc.response().end(config.apkInfo().encode()));
        router.get("/apk").handler(rc -> rc.reroute("/apk/latest"));
        router.get("/apk/:version").handler(rc -> rc.response().sendFile(config.apkFileName(rc.pathParam("version"))));

        final JWTAuth jwtAuth = JWTAuth.create(vertx, config.getJwtAuthOptions());
        router.route("/api/*").handler(JWTAuthHandler.create(jwtAuth));

        router.post("/statisticReport/generate").produces(APPLICATION_JSON)
                .handler(rc -> common(rc, "mes-auto:report:statisticReport:generate"));
        router.post("/statisticReport/fromDisk").produces(APPLICATION_JSON)
                .handler(rc -> common(rc, "mes-auto:report:statisticReport:fromDisk"));
        router.post("/statisticReport/rangeDisk").produces(APPLICATION_JSON)
                .handler(rc -> common(rc, "mes-auto:report:statisticReport:rangeDisk"));

        router.post("/dyeingReport").produces(APPLICATION_JSON)
                .handler(rc -> common(rc, "mes-auto:report:dyeingReport", setMinutes(5)));
        router.post("/strippingReport").produces(APPLICATION_JSON)
                .handler(rc -> common(rc, "mes-auto:report:strippingReport", setMinutes(10)));
        router.post("/measureFiberReport").produces(APPLICATION_JSON)
                .handler(rc -> common(rc, "mes-auto:report:measureFiberReport", setMinutes(5)));
        router.post("/silkExceptionReport").produces(APPLICATION_JSON)
                .handler(rc -> common(rc, "mes-auto:report:silkExceptionReport", setMinutes(3)));


        router.get("/api/reports/doffingSilkCarRecordReport").produces(APPLICATION_JSON).handler(rc -> {
            final JsonObject message = Util.encode(rc);
            final DeliveryOptions deliveryOptions = new DeliveryOptions().setSendTimeout(Duration.ofHours(1).toMillis());
            vertx.eventBus().<String>rxSend("mes-auto:report:doffingSilkCarRecordReport", message.encode(), deliveryOptions)
                    .map(Message::body)
                    .subscribe(rc.response()::end, rc::fail);
        });

        router.get("/share/reports/silkCarRuntimeSilkCarCodes").produces(APPLICATION_JSON).handler(rc -> {
            final JsonObject jsonObject = new JsonObject().put("workshopId", rc.queryParams().get("workshopId"));
            final DeliveryOptions deliveryOptions = new DeliveryOptions().setSendTimeout(Duration.ofHours(1).toMillis());
            vertx.eventBus().<String>rxSend("mes-auto:report:silkCarRuntimeSilkCarCodes", jsonObject.encode(), deliveryOptions)
                    .map(Message::body)
                    .subscribe(rc.response()::end, rc::fail);
        });

        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setDecompressionSupported(true)
                .setCompressionSupported(true);
        return Jvertx.apiGateway().rxMount(router)
                .toSingleDefault(vertx.createHttpServer(httpServerOptions))
                .flatMap(httpServer -> {
                    final int port = config.getPdaConfig().getInteger("port", 9998);
                    return httpServer.requestHandler(router).rxListen(port);
                })
                .ignoreElement();
    }

    private DeliveryOptions setMinutes(long minutes) {
        return new DeliveryOptions().setSendTimeout(Duration.ofMinutes(minutes).toMillis());
    }

    private void common(RoutingContext rc, String address) {
        DeliveryOptions deliveryOptions = new DeliveryOptions().setSendTimeout(Duration.ofMinutes(1).toMillis());
        common(rc, address, deliveryOptions);
    }

    private void common(RoutingContext rc, String address, DeliveryOptions deliveryOptions) {
        vertx.eventBus().rxSend(address, rc.getBodyAsString(), deliveryOptions).subscribe(reply -> {
            String ret = (String) reply.body();
            rc.response().end(ret);
        }, rc::fail);
    }

}
