package com.hengyi.japp.mes.auto.apm.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

import static com.hengyi.japp.mes.auto.apm.Apm.INJECTOR;

/**
 * @author jzb 2018-08-30
 */
@Slf4j
public class WebVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        final Router router = Router.router(vertx);

        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setDecompressionSupported(true)
                .setCompressionSupported(true);
        Jvertx.apiGateway().rxMount(router)
                .toSingleDefault(vertx.createHttpServer(httpServerOptions))
                .flatMap(httpServer -> {
                    final Named portNamed = Names.named("http.port");
                    final Key<Integer> portKey = Key.get(Integer.class, portNamed);
                    return httpServer.requestHandler(router)
                            .rxListen(INJECTOR.getInstance(portKey));
                }).ignoreElement()
                .subscribe(startFuture::complete, startFuture::fail);
    }

}
