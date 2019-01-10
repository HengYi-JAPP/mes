package com.hengyi.japp.mes.auto.worker;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hazelcast.config.Config;
import com.hengyi.japp.mes.auto.worker.verticle.BackendWorkerVerticle;
import com.hengyi.japp.mes.auto.worker.verticle.WorkerVerticle;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.plugins.RxJavaPlugins;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author jzb 2018-11-01
 */
@Slf4j
public class Worker {
    public static Injector INJECTOR;

    public static void main(String[] args) {
        Vertx.rxClusteredVertx(vertxOptions()).flatMapCompletable(vertx -> {
            INJECTOR = Guice.createInjector(new WorkerModule(vertx));

            RxJavaPlugins.setComputationSchedulerHandler(s -> RxHelper.scheduler(vertx));
            RxJavaPlugins.setIoSchedulerHandler(s -> RxHelper.blockingScheduler(vertx));
            RxJavaPlugins.setNewThreadSchedulerHandler(s -> RxHelper.scheduler(vertx));

//            final AuthService authService = Jvertx.getProxy(AuthService.class);
//            final TokenCommand tokenCommand = new TokenCommand();
//            tokenCommand.setLoginId("12000077");
//            tokenCommand.setLoginPassword("123456");
//            authService.token(tokenCommand).subscribe(System.out::println);

            final Completable agent$ = deployAgentWorker(vertx).ignoreElement();
            final Completable backend$ = deployBackendWorker(vertx).ignoreElement();
            return Completable.mergeArray(agent$, backend$);
        }).subscribe();
    }

    private static Single<String> deployAgentWorker(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setInstances(32)
                .setWorker(true);
        return vertx.rxDeployVerticle(WorkerVerticle.class.getName(), deploymentOptions);
    }

    private static Single<String> deployBackendWorker(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setInstances(1)
                .setWorker(true);
        return vertx.rxDeployVerticle(BackendWorkerVerticle.class.getName(), deploymentOptions);
    }

    @SneakyThrows
    private static VertxOptions vertxOptions() {
        final Config config = new Config();
        config.getGroupConfig().setName("mes-auto-cluster");
        final HazelcastClusterManager hazelcastClusterManager = new HazelcastClusterManager(config);

        return new VertxOptions()
                .setClusterManager(hazelcastClusterManager)
                .setClusterHost(InetAddress.getLocalHost().getHostAddress())
                .setWorkerPoolSize(10_000)
                .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(6))
                .setMaxWorkerExecuteTime(TimeUnit.HOURS.toNanos(1));
    }
}
