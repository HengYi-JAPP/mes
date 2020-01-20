package com.hengyi.japp.mes.auto.report.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.FileUpload;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CookieHandler;
import io.vertx.reactivex.ext.web.handler.ResponseContentTypeHandler;
import org.apache.commons.io.FileUtils;

import java.net.URLEncoder;
import java.time.Duration;

import static com.hengyi.japp.mes.auto.Constant.JWT_ALGORITHM;
import static com.hengyi.japp.mes.auto.Util.commonSend;
import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

/**
 * @author jzb 2019-05-20
 */
public class AgentVerticle extends AbstractVerticle {
    @Override
    public Completable rxStart() {
        final MesAutoConfig config = INJECTOR.getInstance(MesAutoConfig.class);

        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create().setUploadsDirectory(FileUtils.getTempDirectoryPath()));
        router.route().handler(ResponseContentTypeHandler.create());
        router.route().handler(CookieHandler.create());
        Jvertx.enableCors(router, config.getCorsConfig().getDomainPatterns());
        router.route().failureHandler(Jvertx::failureHandler);

        router.delete("/admin/cache").handler(rc -> {
            QueryService.CACHE.cleanUp();
            rc.response().end();
        });

        router.get("/downlaods/:downloadToken").produces(APPLICATION_OCTET_STREAM).handler(rc -> {
            final JWTAuth jwtAuth = INJECTOR.getInstance(JWTAuth.class);
            final JWTOptions options = new JWTOptions().setAlgorithm(JWT_ALGORITHM);
            final JsonObject claims = new JsonObject().put("path", "")
                    .put("fileName", "");
            final String downloadToken = jwtAuth.generateToken(claims, options);
            jwtAuth.rxAuthenticate(new JsonObject().put("jwt", rc.pathParam("downloadToken")))
                    .map(User::principal)
                    .subscribe(it -> {
                        final String path = it.getString("path");
                        final String fileName = it.getString("fileName");
                        rc.response().sendFile(path).putHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, UTF_8));
                    }, rc::fail);
        });

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
                .handler(rc -> commonSend(rc, "mes-auto:report:silkExceptionReport", Duration.ofHours(1)));
        router.post("/api/reports/silkExceptionByClassReport").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:silkExceptionByClassReport", Duration.ofHours(1)));

        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setDecompressionSupported(true)
                .setCompressionSupported(true);
        return vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .rxListen(9090)
                .ignoreElement();
    }

}
