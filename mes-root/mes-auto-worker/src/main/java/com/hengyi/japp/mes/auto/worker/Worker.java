package com.hengyi.japp.mes.auto.worker;

import com.github.ixtf.japp.core.J;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.application.command.SilkBarcodeGenerateCommand;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.interfaces.rest.SilkBarcodeResource;
import com.hengyi.japp.mes.auto.worker.verticle.WorkerVerticle;
import com.sun.security.auth.UserPrincipal;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.Vertx;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

            final var principal = new UserPrincipal("5c04a044c3cae813b530cdd1");
            final var command = new SilkBarcodeGenerateCommand();
            command.setCodeDate(new Date());
            command.setDoffingNum("A1");
            final var lineMachine = new EntityDTO();
            lineMachine.setId("5bfd4b87716bb151bd059ded");
            command.setLineMachine(lineMachine);
            final SilkBarcodeResource instance = INJECTOR.getInstance(SilkBarcodeResource.class);
            final ExecutorService executorService = Executors.newFixedThreadPool(5);
            for (int i = 0; i < 5; i++) {
                executorService.submit(() -> {
                    instance.create(principal, command).subscribe(it -> {
                        @NotBlank final String id = it.getId();
                        System.out.println(id);
                    });
                });
            }

//            final ReportService reportService = Jvertx.getProxy(ReportService.class);
//            final LocalDate startLd = LocalDate.of(2019, 1, 14);
//            final LocalDate endLd = LocalDate.of(2019, 1, 14);
//            reportService.statisticsReport("5bffa63d8857b85a437d1fc5", startLd, endLd)
//                    .subscribe(report -> {
//                        System.out.println(report);
//                    });

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

//            final AuthService authService = Jvertx.getProxy(AuthService.class);
//            final TokenCommand tokenCommand = new TokenCommand();
//            tokenCommand.setLoginId("12000077");
//            tokenCommand.setLoginPassword("123456");
//            authService.token(tokenCommand).subscribe(System.out::println);

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
