package com.hengyi.japp.mes.auto.agent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hazelcast.config.Config;
import com.hengyi.japp.mes.auto.agent.verticle.OpenVerticle;
import com.hengyi.japp.mes.auto.agent.verticle.PdaVerticle;
import com.hengyi.japp.mes.auto.agent.verticle.WebVerticle;
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
public class Agent {
    public static Injector INJECTOR;

    public static void main(String[] args) {
        Vertx.rxClusteredVertx(vertxOptions()).flatMapCompletable(vertx -> {
            INJECTOR = Guice.createInjector(new AgentModule(vertx));

            RxJavaPlugins.setComputationSchedulerHandler(s -> RxHelper.scheduler(vertx));
            RxJavaPlugins.setIoSchedulerHandler(s -> RxHelper.blockingScheduler(vertx));
            RxJavaPlugins.setNewThreadSchedulerHandler(s -> RxHelper.scheduler(vertx));

            final Completable web$ = deployWeb(vertx).ignoreElement();
            final Completable pda$ = deployPda(vertx).ignoreElement();
            final Completable open$ = deployOpen(vertx).ignoreElement();
            return Completable.mergeArray(web$, pda$, open$);
        }).subscribe();
    }

    private static Single<String> deployWeb(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        return vertx.rxDeployVerticle(WebVerticle.class.getName(), deploymentOptions);
    }

    private static Single<String> deployPda(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        return vertx.rxDeployVerticle(PdaVerticle.class.getName(), deploymentOptions);
    }

    private static Single<String> deployOpen(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        return vertx.rxDeployVerticle(OpenVerticle.class.getName(), deploymentOptions);
    }

    @SneakyThrows
    private static VertxOptions vertxOptions() {
        final Config config = new Config();
        config.getGroupConfig().setName("mes-auto-cluster");
        final HazelcastClusterManager hazelcastClusterManager = new HazelcastClusterManager(config);

        return new VertxOptions()
                .setClusterManager(hazelcastClusterManager)
                .setClusterHost(InetAddress.getLocalHost().getHostAddress())
//                .setWorkerPoolSize(10_000)
                .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(6))
                .setMaxWorkerExecuteTime(TimeUnit.HOURS.toNanos(1));
    }
}
