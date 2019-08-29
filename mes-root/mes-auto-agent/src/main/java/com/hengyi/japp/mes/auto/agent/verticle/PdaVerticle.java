package com.hengyi.japp.mes.auto.agent.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import io.reactivex.Completable;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import static com.hengyi.japp.mes.auto.Util.commonSend;
import static com.hengyi.japp.mes.auto.agent.Agent.INJECTOR;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

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
                .handler(rc -> commonSend(rc, "mes-auto:report:statisticReport:generate"));
        router.post("/statisticReport/fromDisk").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:statisticReport:fromDisk"));
        router.post("/statisticReport/rangeDisk").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:statisticReport:rangeDisk"));
        router.post("/statisticReport/download").produces(APPLICATION_OCTET_STREAM)
                .handler(rc -> commonSend(rc, "mes-auto:report:statisticReport:download"));

//        router.post("/dyeingReport").produces(APPLICATION_JSON)
//                .handler(rc -> commonSend(rc, "mes-auto:report:dyeingReport", Duration.ofMinutes(5)));
//        router.post("/strippingReport").produces(APPLICATION_JSON)
//                .handler(rc -> commonSend(rc, "mes-auto:report:strippingReport", Duration.ofMinutes(10)));
//        router.post("/measureFiberReport").produces(APPLICATION_JSON)
//                .handler(rc -> commonSend(rc, "mes-auto:report:measureFiberReport", Duration.ofMinutes(5)));
//        router.post("/silkExceptionReport").produces(APPLICATION_JSON)
//                .handler(rc -> commonSend(rc, "mes-auto:report:silkExceptionReport", Duration.ofMinutes(5)));

        router.get("/api/reports/doffingSilkCarRecordReport").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:doffingSilkCarRecordReport", Duration.ofHours(1)));

        router.get("/share/reports/silkCarRuntimeSilkCarCodes").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:silkCarRuntimeSilkCarCodes", Duration.ofHours(1)));

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

}
