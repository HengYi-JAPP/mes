package com.hengyi.japp.mes.auto.agent.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Sets;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeRepository;
import com.hengyi.japp.mes.auto.repository.SilkCarRecordRepository;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import lombok.extern.slf4j.Slf4j;

import static com.hengyi.japp.mes.auto.agent.Agent.INJECTOR;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-06-20
 */
@Slf4j
public class OpenVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        final Router router = Router.router(vertx);
        Jvertx.enableCommon(router);
        Jvertx.enableCors(router, Sets.newHashSet("10\\.2\\.0\\.215"));
        router.route().failureHandler(Jvertx::failureHandler);
        router.route("/eventbus/*").handler(eventBusHandler());

        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setDecompressionSupported(true)
                .setCompressionSupported(true);
        Jvertx.apiGateway().rxMount(router)
                .toSingleDefault(vertx.createHttpServer(httpServerOptions))
                .flatMap(httpServer -> {
                    final Named named = Names.named("open.port");
                    final Key<Integer> key = Key.get(Integer.class, named);
                    final Integer port = INJECTOR.getInstance(key);
                    return httpServer.requestHandler(router).rxListen(port);
                }).ignoreElement()
                .subscribe(startFuture::complete, startFuture::fail);
    }

    private void router(Router router) {
        router.get("/lucences/SilkBarcodes").produces(APPLICATION_JSON).handler(rc -> {
            final SilkBarcodeRepository silkBarcodeRepository = Jvertx.getProxy(SilkBarcodeRepository.class);
            silkBarcodeRepository.list().forEach(silkBarcodeRepository::index);
        });
        router.get("/lucences/SilkCarRecords").produces(APPLICATION_JSON).handler(rc -> {
            final SilkCarRecordRepository silkCarRecordRepository = Jvertx.getProxy(SilkCarRecordRepository.class);
            silkCarRecordRepository.list().forEach(silkCarRecordRepository::index);
        });
        router.get("/lucences/Silks").produces(APPLICATION_JSON).handler(rc -> {
            final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);
            silkRepository.list().forEach(silkRepository::index);
        });
    }

    // todo websocket 权限
    private Handler<RoutingContext> eventBusHandler() {
        final SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(300000);
        final BridgeOptions bo = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddress("sockjs.global"))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("^mes-auto://websocket/boards/.+"));

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
