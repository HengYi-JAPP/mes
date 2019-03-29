package com.hengyi.japp.mes.auto.doffing;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.doffing.verticle.AgentVerticle;
import com.hengyi.japp.mes.auto.doffing.verticle.ScheduleVerticle;
import com.hengyi.japp.mes.auto.doffing.verticle.WorkerVerticle;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jzb 2019-03-07
 */
@Slf4j
public class Doffing {
    public static Injector INJECTOR;

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        INJECTOR = Guice.createInjector(new DoffingModule(vertx));
        final Completable worker = deployWorker(vertx).ignoreElement();
        final Completable agent = deployAgent(vertx).ignoreElement();
        final Completable schedule = deploySchedule(vertx).ignoreElement();
        worker.andThen(agent).andThen(schedule).subscribe();
    }

    private static Single<String> deployWorker(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true);
        return vertx.rxDeployVerticle(WorkerVerticle.class.getName(), deploymentOptions);
    }

    private static Single<String> deployAgent(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true);
        return vertx.rxDeployVerticle(AgentVerticle.class.getName(), deploymentOptions);
    }

    private static Single<String> deploySchedule(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true);
        return vertx.rxDeployVerticle(ScheduleVerticle.class.getName(), deploymentOptions);
    }

}
