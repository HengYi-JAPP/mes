package com.hengyi.japp.mes.auto.report;

import com.github.ixtf.japp.core.J;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.report.verticle.AgentVerticle;
import com.hengyi.japp.mes.auto.report.verticle.WorkerVerticle;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.redis.RedisOptions;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author jzb 2019-02-23
 */
@Slf4j
public class Report {
    public static Injector INJECTOR;
    public static JedisPool JEDIS_POOL;

    public static void main(String[] args) {
        Vertx.rxClusteredVertx(vertxOptions()).flatMapCompletable(vertx -> {
            INJECTOR = Guice.createInjector(new GuiceModule(vertx), new ReportModule());

            final JedisPoolConfig poolConfig = new JedisPoolConfig();
            final RedisOptions redisOptions = INJECTOR.getInstance(MesAutoConfig.class).getRedisOptions();
            poolConfig.setMaxTotal(128);
            poolConfig.setMaxIdle(128);
            poolConfig.setMinIdle(16);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
            poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
            poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
            poolConfig.setNumTestsPerEvictionRun(3);
            poolConfig.setBlockWhenExhausted(true);
            JEDIS_POOL = new JedisPool(poolConfig, redisOptions.getHost(), 6379);

            return Completable.mergeArray(
                    deployWorker(vertx).ignoreElement(),
                    deployAgent(vertx).ignoreElement()
            );
        }).subscribe();
    }

    private static Single<String> deployWorker(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setWorker(true)
                .setMaxWorkerExecuteTime(1)
                .setMaxWorkerExecuteTimeUnit(TimeUnit.HOURS);
        return vertx.rxDeployVerticle(WorkerVerticle.class.getName(), deploymentOptions);
    }

    private static Single<String> deployAgent(Vertx vertx) {
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