package com.hengyi.japp.mes.auto.agent.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.MesAutoConfig;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import lombok.extern.slf4j.Slf4j;

import static com.hengyi.japp.mes.auto.agent.Agent.INJECTOR;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-06-20
 */
@Slf4j
public class PdaVerticle extends AbstractVerticle {
    final MesAutoConfig config = INJECTOR.getInstance(MesAutoConfig.class);

    @Override
    public void start(Future<Void> startFuture) {
        final Router router = Router.router(vertx);
        Jvertx.enableCommon(router);
        Jvertx.enableCors(router, Sets.newHashSet("10\\.2\\.0\\.215"));
        router.route().failureHandler(Jvertx::failureHandler);

        router.get("/apkInfo").produces(APPLICATION_JSON).handler(rc -> rc.response().end(config.apkInfo().encode()));
        router.get("/apk").handler(rc -> rc.reroute("/apk/latest"));
        router.get("/apk/:version").handler(rc -> rc.response().sendFile(config.apkFileName(rc.pathParam("version"))));

        final JWTAuth jwtAuth = JWTAuth.create(vertx, config.getJwtAuthOptions());
        router.route("/api/*").handler(JWTAuthHandler.create(jwtAuth));

        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setDecompressionSupported(true)
                .setCompressionSupported(true);
        Jvertx.apiGateway().rxMount(router)
                .toSingleDefault(vertx.createHttpServer(httpServerOptions))
                .flatMap(httpServer -> {
                    final int port = config.getPdaConfig().getInteger("port", 9998);
                    return httpServer.requestHandler(router).rxListen(port);
                }).ignoreElement()
                .subscribe(startFuture::complete, startFuture::fail);
    }

}
