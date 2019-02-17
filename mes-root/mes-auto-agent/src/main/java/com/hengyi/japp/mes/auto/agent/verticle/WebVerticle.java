package com.hengyi.japp.mes.auto.agent.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Sets;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import lombok.extern.slf4j.Slf4j;

import static com.hengyi.japp.mes.auto.agent.Agent.INJECTOR;

/**
 * @author jzb 2018-08-29
 */
@Slf4j
public class WebVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        final Router router = Router.router(vertx);
        Jvertx.enableCommon(router);
        Jvertx.enableCors(router, Sets.newHashSet("10\\.2\\.0\\.215"));

        final JWTAuth jwtAuth = Jvertx.getProxy(JWTAuth.class);
        router.route("/api/*").handler(JWTAuthHandler.create(jwtAuth));

        router.route("/*").handler(StaticHandler.create());
        router.route("/eventbus/*").handler(eventBusHandler());
        router.route().failureHandler(Jvertx::failureHandler);

        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setDecompressionSupported(true)
                .setCompressionSupported(true);
        final Named portNamed = Names.named("http.port");
        final Key<Integer> portKey = Key.get(Integer.class, portNamed);
        vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .rxListen(INJECTOR.getInstance(portKey))
                .ignoreElement()
                .subscribe(startFuture::complete, startFuture::fail);
    }

    // todo websocket 权限
    private Handler<RoutingContext> eventBusHandler() {
        final SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(300000);
        final BridgeOptions bo = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddress("sockjs.global"))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("^mes-auto://websocket/boards/workshopExceptionReport/.+"));

        return SockJSHandler.create(vertx, options).bridge(bo, be -> {
            if (BridgeEventType.REGISTER == be.type()) {
                System.out.println("rawMessage:" + be.getRawMessage().encode());
//                vertx.setPeriodic(5000, l -> {
//                    Single.just(l)
////                            .subscribeOn(RxHelper.blockingScheduler(vertx))
//                            .subscribeOn(Schedulers.single())
//                            .map(it -> {
//                                System.out.println("map1:" + Thread.currentThread());
//                                return it;
//                            })
//                            .map(it -> {
//                                System.out.println("map2:" + Thread.currentThread());
//                                return it;
//                            })
//                            .observeOn(RxHelper.blockingScheduler(vertx))
//                            .subscribe(it -> {
//                                System.out.println("subscribe:" + Thread.currentThread());
//                            });
//                    be.socket().write(new JsonObject().put("safdasfaf", l).encode());
//                });
            }
            be.complete(true);
        });
    }

}
