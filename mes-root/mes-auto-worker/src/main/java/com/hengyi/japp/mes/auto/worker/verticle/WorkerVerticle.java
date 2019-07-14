package com.hengyi.japp.mes.auto.worker.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
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
        return Jvertx.apiGateway().rxConsume(vertx);
    }

}
