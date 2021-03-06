package com.hengyi.japp.mes.auto.worker;

import com.github.ixtf.japp.core.J;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.worker.verticle.WorkerVerticle;
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

//            final SilkCarRuntimeRepository silkCarRuntimeRepository = Jvertx.getProxy(SilkCarRuntimeRepository.class);
//            silkCarRuntimeRepository.findByCode("3000F48037").subscribe(silkCarRuntime -> {
//                for (SilkRuntime silkRuntime : silkCarRuntime.getSilkRuntimes()) {
//                    try {
//                        final SilkRuntime.DyeingResultInfo firstDyeingResultInfo = silkRuntime.getFirstDyeingResultInfo();
//                        final DyeingResult dyeingResult = firstDyeingResultInfo.getDyeingResult();
//                        final Silk checkSilk = dyeingResult.getSilk();
//                        if (checkSilk == null) {
//                            System.out.println();
//                        }
//                    } catch (Exception e) {
//                        System.out.println(silkCarRuntime);
//                    }
//                }
//            });

            return deployWorker(vertx).ignoreElement();
        }).subscribe();
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
                .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(1000000))
                .setMaxWorkerExecuteTime(TimeUnit.MINUTES.toNanos(5));
        Optional.ofNullable(System.getProperty("vertx.cluster.host"))
                .filter(J::nonBlank)
                .ifPresent(vertxOptions::setClusterHost);
        return vertxOptions;
    }
}
