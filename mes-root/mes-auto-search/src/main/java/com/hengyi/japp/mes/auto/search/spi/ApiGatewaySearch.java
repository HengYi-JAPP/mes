package com.hengyi.japp.mes.auto.search.spi;

import com.github.ixtf.japp.vertx.spi.ApiGateway;
import com.github.ixtf.japp.vertx.spi.internal.AbstractApiGateway;
import com.hengyi.japp.mes.auto.search.Search;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.ext.web.RoutingContext;

import static com.hengyi.japp.mes.auto.search.Search.INJECTOR;

/**
 * @author jzb 2018-11-01
 */
public class ApiGatewaySearch extends AbstractApiGateway implements ApiGateway {
    @Override
    public String addressPrefix() {
        return "mes-auto-search";
    }

    @Override
    protected Flowable<String> rxListPackage() {
        return Flowable.just(Search.class.getPackage().getName());
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
