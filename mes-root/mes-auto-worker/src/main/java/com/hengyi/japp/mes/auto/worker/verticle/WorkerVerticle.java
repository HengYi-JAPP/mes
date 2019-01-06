package com.hengyi.japp.mes.auto.worker.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jzb 2018-08-30
 */
@Slf4j
public class WorkerVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Jvertx.apiGateway().rxConsume(vertx).subscribe(startFuture::complete, startFuture::fail);
    }

}
