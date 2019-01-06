package com.hengyi.japp.mes.auto.print;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.print.application.command.SilkPrintCommand;
import com.hengyi.japp.mes.auto.print.verticle.AmqpVerticle;
import com.hengyi.japp.mes.auto.print.verticle.PrintVerticle;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.plugins.RxJavaPlugins;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author jzb 2018-08-06
 */
@Slf4j
public class Print {
    public static Injector INJECTOR;

    public static void start(String[] args) {
    }

    public static void stop(String[] args) {
        System.exit(0);
    }

    public static void main(String[] args) {
        final VertxOptions vertxOptions = new VertxOptions()
                .setMaxEventLoopExecuteTime(TimeUnit.DAYS.toNanos(1));
        final Vertx vertx = Vertx.vertx(vertxOptions);
        INJECTOR = Guice.createInjector(new PrintModule(vertx));

        RxJavaPlugins.setComputationSchedulerHandler(s -> RxHelper.scheduler(vertx));
        RxJavaPlugins.setIoSchedulerHandler(s -> RxHelper.blockingScheduler(vertx));
        RxJavaPlugins.setNewThreadSchedulerHandler(s -> RxHelper.scheduler(vertx));

        final Completable print$ = deployPrint(vertx).toCompletable();
        final Completable amqp$ = deployAmqp(vertx).toCompletable();
        Completable.mergeArray(print$, amqp$).subscribe(() -> test());
    }

    private static Single<String> deployPrint(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        return vertx.rxDeployVerticle(PrintVerticle.class.getName(), deploymentOptions);
    }

    private static Single<String> deployAmqp(Vertx vertx) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        return vertx.rxDeployVerticle(AmqpVerticle.class.getName(), deploymentOptions);
    }

    static void test() throws Exception {
//        final SilkPrintCommand command = command(10);
//        command.toPrintable().PrintLabel();
    }

    public static SilkPrintCommand command(int count) {
        final SilkPrintCommand command = new SilkPrintCommand();
        final List<SilkPrintCommand.Item> items = IntStream.range(0, count)
                .mapToObj(Print::item)
                .collect(Collectors.toList());
        command.setSilks(items);
        return command;
    }

    public static SilkPrintCommand.Item item(int i) {
        final SilkPrintCommand.Item item = new SilkPrintCommand.Item();
        item.setCodeDate(new Date());
        item.setLineName("B1");
        item.setLineMachineItem(i);
        item.setSpindle(i);
        item.setDoffingNum("A1");
        item.setCode("123456789011");
        item.setBatchNo("setBatchNo");
        item.setBatchSpec("batchSpec");
        return item;
    }
}
