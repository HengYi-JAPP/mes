package com.hengyi.japp.mes.auto.doffing.verticle;

import com.hengyi.japp.mes.auto.doffing.DoffingModuleConfig;
import com.hengyi.japp.mes.auto.doffing.application.DoffingService;
import com.hengyi.japp.mes.auto.doffing.domain.AbstractAutoDoffingSilkCarRecordAdapt;
import com.hengyi.japp.mes.auto.doffing.domain.AutoDoffingSilkCarRecordAdapt;
import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static com.hengyi.japp.mes.auto.doffing.Doffing.INJECTOR;

/**
 * @author jzb 2019-03-08
 */
@Slf4j
public class ScheduleVerticle extends AbstractVerticle {
    private DoffingModuleConfig config = INJECTOR.getInstance(DoffingModuleConfig.class);
    private RabbitMQClient rabbitMQClient = INJECTOR.getInstance(RabbitMQClient.class);
    private DoffingService doffingService = INJECTOR.getInstance(DoffingService.class);

    @Override
    public Completable rxStart() {
        final var declare$ = rabbitMQClient.rxExchangeDeclare(config.getDoffingExchange(), "direct", true, false);
        return rabbitMQClient.rxStart().andThen(declare$).doOnComplete(() -> {
            startFetchSchedule(5000);
            startCleanSchedule(5000);
        });
    }

    private void startCleanSchedule(long delay) {
        vertx.setTimer(delay, l -> doffingService.clean()
                .doAfterTerminate(() -> {
                    final long cleanDelay = config.getCleanDelayDays();
                    startCleanSchedule(TimeUnit.DAYS.toMillis(cleanDelay));
                })
                .doOnComplete(() -> log.info("clean success"))
                .doOnError(err -> log.error("startClean: ", err))
                .subscribe());
    }

    private void startFetchSchedule(long delay) {
        vertx.setTimer(delay, l -> doffingService.fetch()
                .flatMapCompletable(this::handle)
                .doAfterTerminate(() -> {
                    final long fetchDelay = config.getFetchDelaySeconds();
                    startFetchSchedule(TimeUnit.SECONDS.toMillis(fetchDelay));
                })
                .doOnError(err -> log.error("startFetch: ", err))
                .subscribe());
    }

    private Completable handle(AutoDoffingSilkCarRecordAdapt data) {
        final Completable publlish$ = doffingService.toMessageBody(data)
                .flatMapCompletable(it -> {
                    final JsonObject message = new JsonObject().put("body", it);
                    return rabbitMQClient.rxBasicPublish(config.getDoffingExchange(), "", message);
                })
                .doOnComplete(() -> {
                    final StringBuilder sb = logBuilder(data).append("publish success");
                    log.info(sb.toString());
                })
                .doOnError(err -> {
                    final StringBuilder sb = logBuilder(data).append("publish failure");
                    log.error(sb.toString(), err);
                });
        final Completable toHistory$ = doffingService.toHistory(data)
                .doOnSuccess(it -> {
                    final StringBuilder sb = logBuilder(data).append("history success");
                    log.info(sb.toString());
                })
                .doOnError(err -> {
                    final StringBuilder sb = logBuilder(data).append("history failure");
                    log.error(sb.toString(), err);
                })
                .ignoreElement();
        return publlish$.andThen(toHistory$).onErrorComplete();
    }

    private StringBuilder logBuilder(AbstractAutoDoffingSilkCarRecordAdapt data) {
        return new StringBuilder("车次[").append(data.getId()).append("],丝车[").append(data.getCode()).append("]: ");
    }

}
