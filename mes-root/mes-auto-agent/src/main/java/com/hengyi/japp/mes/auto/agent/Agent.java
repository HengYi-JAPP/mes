package com.hengyi.japp.mes.auto.agent;

import com.github.ixtf.japp.core.J;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.agent.verticle.MqttVerticle;
import com.hengyi.japp.mes.auto.agent.verticle.OpenVerticle;
import com.hengyi.japp.mes.auto.agent.verticle.PdaVerticle;
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
public class Agent {
    public static Injector INJECTOR;

    public static void main(String[] args) {
        Vertx.rxClusteredVertx(vertxOptions()).flatMapCompletable(vertx -> {
            INJECTOR = Guice.createInjector(new GuiceModule(vertx));

            return Completable.mergeArray(
                    deployMqtt(vertx).ignoreElement(),
                    deployPda(vertx).ignoreElement(),
                    deployOpen(vertx).ignoreElement()
            );
        }).subscribe(() -> {
            log.info("mes-auto Agent 启动成功");
        }, err -> {
            log.info("mes-auto Agent 启动失败");
        });
    }

    private static Single<String> deployMqtt(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        return vertx.rxDeployVerticle(MqttVerticle.class.getName(), deploymentOptions);
    }

    private static Single<String> deployPda(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions().setInstances(24);
        return vertx.rxDeployVerticle(PdaVerticle.class.getName(), deploymentOptions);
    }

    private static Single<String> deployOpen(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions().setInstances(8);
        return vertx.rxDeployVerticle(OpenVerticle.class.getName(), deploymentOptions);
    }

    @SneakyThrows
    private static VertxOptions vertxOptions() {
        final VertxOptions vertxOptions = new VertxOptions()
                .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(10));
        Optional.ofNullable(System.getProperty("vertx.cluster.host")).filter(J::nonBlank)
                .ifPresent(vertxOptions.getEventBusOptions()::setHost);
        return vertxOptions;
    }
}
