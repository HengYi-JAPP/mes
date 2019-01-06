package com.hengyi.japp.mes.auto.apm.spi;

import com.github.ixtf.japp.vertx.spi.ApiGateway;
import com.github.ixtf.japp.vertx.spi.internal.AbstractApiGateway;
import com.hengyi.japp.mes.auto.apm.Apm;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.ext.web.RoutingContext;

import static com.hengyi.japp.mes.auto.apm.Apm.INJECTOR;

/**
 * @author jzb 2018-11-01
 */
public class ApiGatewayApm extends AbstractApiGateway implements ApiGateway {
    @Override
    public String addressPrefix() {
        return "mes-auto-apm";
    }

    @Override
    protected Flowable<String> rxListPackage() {
        return Flowable.just(Apm.class.getPackage().getName());
    }

    @Override
    public Single<String> rxPrincipal(RoutingContext rc) {
        return Single.error(new IllegalAccessException());
    }

    @Override
    public <T> T getProxy(Class<T> clazz) {
        return INJECTOR.getInstance(clazz);
    }
}
