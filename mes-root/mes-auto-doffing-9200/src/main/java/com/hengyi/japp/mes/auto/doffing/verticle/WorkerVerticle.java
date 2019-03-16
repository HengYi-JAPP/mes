package com.hengyi.japp.mes.auto.doffing.verticle;

import com.github.ixtf.vertx.Jvertx;
import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jzb 2018-08-30
 */
@Slf4j
public class WorkerVerticle extends AbstractVerticle {

    @Override
    public Completable rxStart() {
        return Completable.mergeArray(Jvertx.routes().map(route ->
                vertx.eventBus().consumer(route.getAddress(), route.getMessageHandler()).rxCompletionHandler()
        ).toArray(Completable[]::new));
    }

}
