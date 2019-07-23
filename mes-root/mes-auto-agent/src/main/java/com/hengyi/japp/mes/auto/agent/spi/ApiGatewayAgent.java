package com.hengyi.japp.mes.auto.agent.spi;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.dto.ApmTraceSpan;
import com.github.ixtf.japp.vertx.spi.ApiGateway;
import com.github.ixtf.japp.vertx.spi.internal.AbstractApiGateway;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.web.RoutingContext;

import java.util.Optional;

import static com.hengyi.japp.mes.auto.agent.Agent.INJECTOR;

/**
 * @author jzb 2018-11-01
 */
public class ApiGatewayAgent extends AbstractApiGateway implements ApiGateway {

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
        return Optional.ofNullable(rc.user())
                .map(User::principal)
                .map(it -> it.getString("uid"))
                .filter(J::nonBlank)
                .map(Single::just)
                .orElse(Single.just(""));
    }

    @Override
    public void submitApmSpan(ApmTraceSpan apmSpan) {
//        System.out.println(apmSpan);
    }

    @Override
    public <T> T getProxy(Class<T> clazz) {
        return INJECTOR.getInstance(clazz);
    }
}
