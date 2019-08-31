package com.hengyi.japp.mes.auto.report.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import io.reactivex.Completable;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;

import java.time.Duration;

import static com.hengyi.japp.mes.auto.Util.commonSend;
import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2019-05-20
 */
public class AgentVerticle extends AbstractVerticle {
    @Override
    public Completable rxStart() {
        final MesAutoConfig config = INJECTOR.getInstance(MesAutoConfig.class);

        final Router router = Router.router(vertx);
        Jvertx.enableCommon(router);
        Jvertx.enableCors(router, config.getCorsConfig().getDomainPatterns());
        router.route().failureHandler(Jvertx::failureHandler);

        router.delete("/admin/cache").handler(rc -> {
            QueryService.CACHE.cleanUp();
            rc.response().end();
        });

        router.get("/api/reports/doffingSilkCarRecordReport").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:doffingSilkCarRecordReport", Duration.ofHours(1)));

        router.get("/share/reports/silkCarRuntimeSilkCarCodes").produces(APPLICATION_JSON)
                .handler(rc -> commonSend(rc, "mes-auto:report:silkCarRuntimeSilkCarCodes", Duration.ofHours(1)));

        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setDecompressionSupported(true)
                .setCompressionSupported(true);
        return vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .rxListen(9090)
                .ignoreElement();
    }

}
