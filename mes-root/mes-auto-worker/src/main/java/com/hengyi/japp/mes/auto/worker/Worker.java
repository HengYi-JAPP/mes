package com.hengyi.japp.mes.auto.worker;

import com.github.ixtf.japp.core.J;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.worker.verticle.RuiguanAutoDoffingVerticle;
import com.hengyi.japp.mes.auto.worker.verticle.WorkerVerticle;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.Vertx;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author jzb 2018-11-01
 */
@Slf4j
public class Worker {
    public static Injector INJECTOR;

    public static void main(String[] args) {
        Vertx.rxClusteredVertx(vertxOptions()).flatMapCompletable(vertx -> {
            INJECTOR = Guice.createInjector(new GuiceModule(vertx), new WorkerModule());

            return Completable.mergeArray(
//                    deployRuiguan(vertx).ignoreElement()
                    deployWorker(vertx).ignoreElement()
            );
        }).subscribe();
    }

    private static Single<String> deployRuiguan(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setWorker(true);
        return vertx.rxDeployVerticle(RuiguanAutoDoffingVerticle.class.getName(), deploymentOptions);
    }

    private static Single<String> deployWorker(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setInstances(1000)
                .setWorker(true);
        return vertx.rxDeployVerticle(WorkerVerticle.class.getName(), deploymentOptions);
    }

    @SneakyThrows
    private static VertxOptions vertxOptions() {
        final VertxOptions vertxOptions = new VertxOptions()
                .setWorkerPoolSize(1000)
                .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(1_000_000))
                .setMaxWorkerExecuteTime(TimeUnit.MINUTES.toNanos(5));
        Optional.ofNullable(System.getProperty("vertx.cluster.host"))
                .filter(J::nonBlank)
                .ifPresent(vertxOptions.getEventBusOptions()::setHost);
        return vertxOptions;
    }

}
