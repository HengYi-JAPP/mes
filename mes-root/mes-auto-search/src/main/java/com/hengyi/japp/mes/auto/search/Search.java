package com.hengyi.japp.mes.auto.search;

import com.github.ixtf.japp.core.J;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.search.verticle.SearchVerticle;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.Vertx;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author jzb 2018-12-13
 */
public class Search {
    public static Injector INJECTOR;

    public static void main(String[] args) {
        Vertx.rxClusteredVertx(vertxOptions()).flatMapCompletable(vertx -> {
            INJECTOR = Guice.createInjector(new GuiceModule(vertx));
            return deploySearch(vertx).ignoreElement();
        });
    }

    private static Single<String> deploySearch(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setInstances(1000)
                .setWorker(true);
        return vertx.rxDeployVerticle(SearchVerticle.class.getName(), deploymentOptions);
    }

    @SneakyThrows
    private static VertxOptions vertxOptions() {
        final VertxOptions vertxOptions = new VertxOptions()
                .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(6))
                .setWorkerPoolSize(1000)
                .setMaxWorkerExecuteTime(TimeUnit.MINUTES.toNanos(30));
        Optional.ofNullable(System.getProperty("vertx.cluster.host"))
                .filter(J::nonBlank)
                .ifPresent(vertxOptions::setClusterHost);
        return vertxOptions;
    }

}
