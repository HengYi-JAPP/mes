package com.hengyi.japp.mes.auto.worker.spi;

import com.github.ixtf.japp.vertx.dto.ApmTraceSpan;
import com.github.ixtf.japp.vertx.spi.ApiGateway;
import com.github.ixtf.japp.vertx.spi.internal.AbstractApiGateway;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.ext.web.RoutingContext;

import static com.hengyi.japp.mes.auto.worker.Worker.INJECTOR;

/**
 * @author jzb 2018-11-01
 */
public class ApiGatewayWorker extends AbstractApiGateway implements ApiGateway {

    @Override
    public String addressPrefix() {
        return "mes-auto";
    }

    @Override
    protected Flowable<String> rxListPackage() {
        return Flowable.just("com.hengyi.japp.mes.auto");
    }

    @Override
    public Single<String> rxPrincipal(RoutingContext rc) {
        return Single.error(new IllegalAccessException());
    }

    @Override
    public void submitApmSpan(ApmTraceSpan apmSpan) {
    }

    @Override
    public <T> T getProxy(Class<T> clazz) {
        return INJECTOR.getInstance(clazz);
    }

}
