package com.hengyi.japp.mes.auto.report;

import com.github.ixtf.japp.core.J;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.GuiceModule;
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

    public static void main(String[] args) {
        Vertx.rxClusteredVertx(vertxOptions()).flatMapCompletable(vertx -> {
            INJECTOR = Guice.createInjector(new GuiceModule(vertx), new ReportModule());

            return Completable.mergeArray(
                    deployReport(vertx).ignoreElement()
            );
        }).subscribe();
    }

    private static Single<String> deployReport(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true);
        return vertx.rxDeployVerticle(ReportVerticle.class.getName(), deploymentOptions);
    }

    @SneakyThrows
    private static VertxOptions vertxOptions() {
        final VertxOptions vertxOptions = new VertxOptions()
                .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(1000000))
                .setMaxWorkerExecuteTime(TimeUnit.MINUTES.toNanos(10));
        Optional.ofNullable(System.getProperty("vertx.cluster.host")).filter(J::nonBlank)
                .ifPresent(vertxOptions.getEventBusOptions()::setHost);
        return vertxOptions;
    }

    public static MongoCollection<Document> mongoCollection(Class<?> clazz) {
        final MongoDatabase mongoDatabase = INJECTOR.getInstance(MongoDatabase.class);
        return mongoDatabase.getCollection("T_" + clazz.getSimpleName());
    }

}