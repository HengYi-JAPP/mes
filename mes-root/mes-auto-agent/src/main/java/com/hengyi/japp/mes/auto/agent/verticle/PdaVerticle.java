package com.hengyi.japp.mes.auto.agent.verticle;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import io.reactivex.Completable;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.hengyi.japp.mes.auto.agent.Agent.INJECTOR;
import static java.util.stream.Collectors.toMap;
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

        router.post("/dyeingReport").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:dyeingReport", Duration.ofMinutes(5)));
        router.post("/strippingReport").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:strippingReport", Duration.ofMinutes(10)));
        router.post("/measureFiberReport").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:measureFiberReport", Duration.ofMinutes(5)));
        router.post("/silkExceptionReport").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:silkExceptionReport", Duration.ofMinutes(5)));


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
        return Jvertx.apiGateway().rxMount(router)
                .toSingleDefault(vertx.createHttpServer(httpServerOptions))
                .flatMap(httpServer -> {
                    final int port = config.getPdaConfig().getInteger("port", 9998);
                    return httpServer.requestHandler(router).rxListen(port);
                })
                .ignoreElement();
    }

    private void commonSend(RoutingContext rc, String address) {
        commonSend(rc, address, new DeliveryOptions());
    }

    private void commonSend(RoutingContext rc, String address, Duration duration) {
        final DeliveryOptions deliveryOptions = new DeliveryOptions().setSendTimeout(duration.toMillis());
        commonSend(rc, address, deliveryOptions);
    }

    private void commonSend(RoutingContext rc, String address, DeliveryOptions deliveryOptions) {
        vertx.eventBus().rxSend(address, encode(rc), deliveryOptions).subscribe(reply -> {
            final HttpServerResponse response = rc.response();
            final MultiMap headers = reply.headers();
            headers.entries().forEach(it -> response.putHeader(it.getKey(), it.getValue()));
            response.end(buffer(reply));
        }, rc::fail);
    }

    private JsonObject encode(RoutingContext rc) {
        final JsonObject principal = Optional.ofNullable(rc.user()).map(User::principal).orElse(null);
        final Map<String, String> pathParams = rc.pathParams();
        final Map<String, List<String>> queryParams = rc.queryParams().names().parallelStream().collect(toMap(Function.identity(), rc.queryParams()::getAll));
        return new JsonObject().put("principal", principal)
                .put("pathParams", pathParams)
                .put("queryParams", queryParams)
                .put("body", rc.getBodyAsString());
    }

    private Buffer buffer(Message<Object> reply) {
        final Object body = reply.body();
        if (body == null) {
            return Buffer.buffer();
        }
        if (body instanceof String) {
            final String result = (String) body;
            if (J.isBlank(result)) {
                return Buffer.buffer();
            } else {
                return Buffer.buffer(result);
            }
        }
        final byte[] bytes = (byte[]) body;
        return Buffer.buffer(bytes);
    }

}
