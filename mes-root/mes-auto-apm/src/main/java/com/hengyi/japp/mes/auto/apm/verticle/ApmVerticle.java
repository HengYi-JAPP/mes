package com.hengyi.japp.mes.auto.apm.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.apm.application.ApmService;
import com.hengyi.japp.mes.auto.verticle.BaseAmqpVerticle;
import io.reactivex.Completable;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import static com.hengyi.japp.mes.auto.Constant.AMQP.MES_AUTO_APM_EXCHANGE;

/**
 * @author jzb 2018-08-30
 */
@Slf4j
public class ApmVerticle extends BaseAmqpVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        final Completable binds$ = Completable.mergeArray(
                logService()
        );
        ensureExchange().andThen(binds$)
                .subscribe(startFuture::complete, startFuture::fail);
    }

    private Completable logService() {
        final ApmService apmService = Jvertx.getProxy(ApmService.class);
        return consumer(ApmService.ADDRESS, MES_AUTO_APM_EXCHANGE, ApmService.ADDRESS, apmService::handleLog);
    }

}
