package com.hengyi.japp.mes.auto.agent.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.FileUpload;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CookieHandler;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import io.vertx.reactivex.ext.web.handler.ResponseContentTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

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
        router.route().handler(BodyHandler.create().setUploadsDirectory(FileUtils.getTempDirectoryPath()));
        router.route().handler(ResponseContentTypeHandler.create());
        router.route().handler(CookieHandler.create());
        Jvertx.enableCors(router, config.getCorsConfig().getDomainPatterns());
        router.route().failureHandler(Jvertx::failureHandler);

        router.get("/apkInfo").produces(APPLICATION_JSON).handler(rc -> rc.response().end(config.apkInfo().encode()));
        router.get("/apk").handler(rc -> rc.reroute("/apk/latest"));
        router.get("/apk/:version").handler(rc -> rc.response().sendFile(config.apkFileName(rc.pathParam("version"))));

        final JWTAuth jwtAuth = JWTAuth.create(vertx, config.getJwtAuthOptions());
        router.route("/api/*").handler(JWTAuthHandler.create(jwtAuth));

        router.post("/api/reports/statisticReport/generate").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:statisticReport:generate", Duration.ofMinutes(5)));
        router.post("/api/reports/statisticReport/fromDisk").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:statisticReport:fromDisk"));
        router.post("/api/reports/statisticReport/rangeDisk").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:statisticReport:rangeDisk"));
        router.get("/api/reports/statisticReport/download").produces(APPLICATION_OCTET_STREAM)
                .handler(rc -> commonSend(rc, "mes-auto:report:statisticReport:download"));
        router.post("/api/reports/statisticReport/combines").produces(APPLICATION_OCTET_STREAM).handler(rc -> vertx.rxExecuteBlocking(f -> Flowable.fromIterable(rc.fileUploads())
                .map(FileUpload::uploadedFileName)
                .flatMapSingle(vertx.fileSystem()::rxReadFile)
                .map(Buffer::getBytes)
                .toList()
                .map(list -> {
                    final JsonArray array = new JsonArray();
                    list.stream().forEach(array::add);
                    return array;
                })
                .subscribe(f::complete, f::fail), false)
                .flatMapSingle(it -> vertx.eventBus().<byte[]>rxSend("mes-auto:report:statisticReport:combines", it))
                .subscribe(reply -> {
                    final HttpServerResponse response = rc.response();
                    final MultiMap headers = reply.headers();
                    headers.entries().forEach(it -> response.putHeader(it.getKey(), it.getValue()));
                    response.end(Buffer.buffer(reply.body()));
                }, rc::fail));

        router.post("/api/reports/strippingReport").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:strippingReport", Duration.ofMinutes(10)));
        router.post("/api/reports/inspectionReport").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:inspectionReport", Duration.ofMinutes(10)));
        router.post("/api/reports/dyeingReport").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:dyeingReport", Duration.ofMinutes(5)));
        router.post("/api/reports/measureFiberReport").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:measureFiberReport", Duration.ofMinutes(5)));
        router.post("/api/reports/silkExceptionReport").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:silkExceptionReport", Duration.ofMinutes(5)));

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
