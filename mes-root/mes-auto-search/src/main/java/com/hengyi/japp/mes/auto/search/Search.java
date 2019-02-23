package com.hengyi.japp.mes.auto.search;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.search.verticle.SearchVerticle;
import com.hengyi.japp.mes.auto.search.verticle.WebVerticle;
import io.reactivex.Completable;
import io.reactivex.plugins.RxJavaPlugins;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;

/**
 * @author jzb 2018-12-13
 */
public class Search {
    public static Injector INJECTOR;

    public static void main(String[] args) {
        final VertxOptions vertxOptions = new VertxOptions()
                .setWorkerPoolSize(10_000)
                .setHAEnabled(true);
        final Vertx vertx = Vertx.vertx(vertxOptions);
        INJECTOR = Guice.createInjector(new GuiceModule(vertx));

        RxJavaPlugins.setComputationSchedulerHandler(s -> RxHelper.scheduler(vertx));
        RxJavaPlugins.setIoSchedulerHandler(s -> RxHelper.blockingScheduler(vertx));
        RxJavaPlugins.setNewThreadSchedulerHandler(s -> RxHelper.scheduler(vertx));

        DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setWorker(true)
                .setInstances(32);
        final Completable search$ = vertx.rxDeployVerticle(SearchVerticle.class.getName(), deploymentOptions).ignoreElement();

        deploymentOptions = new DeploymentOptions()
                .setInstances(32);
        final Completable web$ = vertx.rxDeployVerticle(WebVerticle.class.getName(), deploymentOptions).ignoreElement();

        search$.andThen(web$).subscribe();
    }

}
