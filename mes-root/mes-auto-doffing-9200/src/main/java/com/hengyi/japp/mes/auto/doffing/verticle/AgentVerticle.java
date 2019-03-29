package com.hengyi.japp.mes.auto.doffing.verticle;

import com.github.ixtf.vertx.Jvertx;
import com.hengyi.japp.mes.auto.doffing.DoffingModuleConfig;
import io.reactivex.Completable;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Route;
import io.vertx.reactivex.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

import static com.hengyi.japp.mes.auto.doffing.Doffing.INJECTOR;

/**
 * @author jzb 2018-08-30
 */
@Slf4j
public class AgentVerticle extends AbstractVerticle {
    private DoffingModuleConfig config = INJECTOR.getInstance(DoffingModuleConfig.class);

    @Override
    public Completable rxStart() {
        final Router router = Router.router(vertx);
        Jvertx.enableCommon(router);
        router.route().failureHandler(Jvertx::failureHandler);

        Jvertx.routes().forEach(routeRepresentation -> {
            final Route route = router.route(routeRepresentation.getHttpMethod(), routeRepresentation.getPath());
            routeRepresentation.consumes().forEach(route::consumes);
            routeRepresentation.produces().forEach(route::produces);
            route.handler(routeRepresentation.getRoutingContextHandler());
        });

        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setDecompressionSupported(true)
                .setCompressionSupported(true);
        final Integer port = config.getApiConfig().getInteger("port", 8080);
        return vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .rxListen(port)
                .ignoreElement();
    }

}
