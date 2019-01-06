package com.hengyi.japp.mes.auto.print.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;

import static com.hengyi.japp.mes.auto.print.Print.INJECTOR;

/**
 * @author jzb 2018-04-18
 */
public class PrintVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        final Router router = Router.router(vertx);
        Jvertx.enableCommon(router, LocalSessionStore.create(vertx));
        router.route().failureHandler(Jvertx::failureHandler);

        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setDecompressionSupported(true)
                .setCompressionSupported(true);
        Jvertx.apiGateway().rxMount(router)
                .toSingleDefault(vertx.createHttpServer(httpServerOptions))
                .flatMap(httpServer -> {
                    final Named named = Names.named("http.port");
                    final Key<Integer> key = Key.get(Integer.class, named);
                    final Integer port = INJECTOR.getInstance(key);
                    return httpServer.requestHandler(router::accept).rxListen(port);
                }).toCompletable()
                .subscribe(startFuture::complete, startFuture::fail);
    }

}
