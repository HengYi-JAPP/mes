package com.hengyi.japp.mes.auto.report.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import io.reactivex.Completable;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.web.Router;

import java.time.Duration;

import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2019-05-20
 */
public class AgentVerticle extends AbstractVerticle {
    @Override
    public Completable rxStart() {
        final MesAutoConfig config = INJECTOR.getInstance(MesAutoConfig.class);

        final Router router = Router.router(vertx);
        Jvertx.enableCommon(router);
        Jvertx.enableCors(router, config.getCorsConfig().getDomainPatterns());
        router.route().failureHandler(Jvertx::failureHandler);

        router.delete("/admin/cache").handler(rc -> {
            QueryService.CACHE.cleanUp();
            rc.response().end();
        });

        router.get("/api/reports/doffingSilkCarRecordReport").produces(APPLICATION_JSON).handler(rc -> {
            final JsonObject message = new JsonObject()
                    .put("workshopId", rc.queryParams().get("workshopId"))
                    .put("startDate", rc.queryParams().get("startDate"))
                    .put("endDate", rc.queryParams().get("endDate"));
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
        return vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .rxListen(9090)
                .ignoreElement();
    }

}
