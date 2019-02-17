package com.hengyi.japp.mes.auto.worker.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jzb 2018-08-30
 */
@Slf4j
public class WorkerVerticle extends AbstractVerticle {
    private static final AtomicInteger deployCount = new AtomicInteger();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        System.out.println(getClass().getSimpleName() + " count: " + deployCount.incrementAndGet());

        Jvertx.apiGateway().rxConsume(vertx).subscribe(startFuture::complete, startFuture::fail);
    }

}
