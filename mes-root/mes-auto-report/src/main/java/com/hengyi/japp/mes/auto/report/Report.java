package com.hengyi.japp.mes.auto.report;

import com.github.ixtf.japp.core.J;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.report.verticle.AgentVerticle;
import com.hengyi.japp.mes.auto.report.verticle.WorkerVerticle;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.Vertx;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author jzb 2019-02-23
 */
@Slf4j
public class Report {
    public static Injector INJECTOR;

    //    static {
//        // sshfs -o allow_other root@10.2.0.215:/data/mes/auto/db /data/mes-3000/auto/db
//        System.setProperty("japp.mes.auto.path", "/data/mes-3000/auto");
////        INJECTOR = Guice.createInjector(new GuiceModule(vertx), new ReportModule());
//    }
    public static void main(String[] args) {
        Vertx.rxClusteredVertx(vertxOptions()).flatMapCompletable(vertx -> {
            INJECTOR = Guice.createInjector(new GuiceModule(vertx), new ReportModule());

            return Completable.mergeArray(
                    deployWorker(vertx).ignoreElement(),
                    deployAgent(vertx).ignoreElement()
            );
        }).subscribe();
    }

    public static Single<String> deployWorker(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setWorker(true)
                .setMaxWorkerExecuteTime(1)
                .setMaxWorkerExecuteTimeUnit(TimeUnit.HOURS)
                .setInstances(1000);
        return vertx.rxDeployVerticle(WorkerVerticle.class.getName(), deploymentOptions);
    }

    public static Single<String> deployAgent(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions().setInstances(4);
        return vertx.rxDeployVerticle(AgentVerticle.class.getName(), deploymentOptions);
    }

    @SneakyThrows
    private static VertxOptions vertxOptions() {
        final VertxOptions vertxOptions = new VertxOptions()
                .setMaxWorkerExecuteTime(1)
                .setMaxWorkerExecuteTimeUnit(TimeUnit.HOURS);
        Optional.ofNullable(System.getProperty("vertx.cluster.host")).filter(J::nonBlank)
                .ifPresent(vertxOptions.getEventBusOptions()::setHost);
        return vertxOptions;
    }

    public static MongoCollection<Document> mongoCollection(Class<?> clazz) {
        final MongoDatabase mongoDatabase = INJECTOR.getInstance(MongoDatabase.class);
        return mongoDatabase.getCollection("T_" + clazz.getSimpleName());
    }

}