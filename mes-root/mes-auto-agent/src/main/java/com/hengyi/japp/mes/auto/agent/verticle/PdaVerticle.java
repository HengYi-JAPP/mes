package com.hengyi.japp.mes.auto.agent.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.hengyi.japp.mes.auto.agent.Agent.INJECTOR;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-06-20
 */
@Slf4j
public class PdaVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        final Router router = Router.router(vertx);
        Jvertx.enableCommon(router);
        router.route().failureHandler(Jvertx::failureHandler);
        router(router);
        final JWTAuth jwtAuth = Jvertx.getProxy(JWTAuth.class);
        router.route("/api/*").handler(JWTAuthHandler.create(jwtAuth));

        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setDecompressionSupported(true)
                .setCompressionSupported(true);
        Jvertx.apiGateway().rxMount(router)
                .toSingleDefault(vertx.createHttpServer(httpServerOptions))
                .flatMap(httpServer -> {
                    final Named named = Names.named("pda.port");
                    final Key<Integer> key = Key.get(Integer.class, named);
                    final Integer port = INJECTOR.getInstance(key);
                    return httpServer.requestHandler(router).rxListen(port);
                }).ignoreElement()
                .subscribe(startFuture::complete, startFuture::fail);
    }

    private void router(Router router) {
        router.get("/apkInfo").produces(APPLICATION_JSON).handler(rc -> {
            final JsonObject apkInfo = apkInfo();
            rc.response().end(apkInfo.encode());
        });
        router.get("/apk").handler(rc -> rc.reroute("/apk/latest"));
        router.get("/apk/:version").handler(rc -> {
            final HttpServerRequest request = rc.request();
            String version = request.getParam("version");
            final Named named = Names.named("autoRootPath");
            final Key<Path> key = Key.get(Path.class, named);
            final Path autoRootPath = INJECTOR.getInstance(key);
            if (version.equalsIgnoreCase("latest")) {
                final JsonObject apkInfo = apkInfo();
                version = apkInfo.getString("version");
            }
            final Path apkPath = autoRootPath.resolve(Paths.get("apk", version, "mes-auto.apk"));
            rc.response().sendFile(apkPath.toFile().getPath());
        });
    }

    private JsonObject apkInfo() {
        final Named named = Names.named("apkInfo");
        final Key<JsonObject> key = Key.get(JsonObject.class, named);
        return INJECTOR.getInstance(key);
    }

}
