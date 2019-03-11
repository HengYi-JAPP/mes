package com.hengyi.japp.mes.auto.worker.verticle;

import com.hengyi.japp.mes.auto.application.SilkCarRecordService;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent.AutoDoffingSilkCarRuntimeCreateCommand;
import io.reactivex.Completable;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import io.vertx.reactivex.rabbitmq.RabbitMQConsumer;
import lombok.extern.slf4j.Slf4j;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.worker.Worker.INJECTOR;

/**
 * @author jzb 2019-03-09
 */
@Slf4j
public class AutoDoffingEventVerticle extends AbstractVerticle {
    private final String ex_name = "mes.auto.doffing.9200";
    private final String queue_name = "generate-silk-car-record";
    private RabbitMQClient rabbitMQClient = INJECTOR.getInstance(RabbitMQClient.class);

    @Override
    public Completable rxStart() {
        final Completable declare$ = rabbitMQClient.rxQueueDeclare(queue_name, true, false, false).ignoreElement();
        final Completable bind$ = rabbitMQClient.rxQueueBind(queue_name, ex_name, "");
        final QueueOptions queueOptions = new QueueOptions().setAutoAck(false);
        final Completable consumer$ = rabbitMQClient.rxBasicConsumer(queue_name, queueOptions)
                .doOnSuccess(this::handleConsumer)
                .ignoreElement();
        return rabbitMQClient.rxStart().andThen(declare$).andThen(bind$).andThen(consumer$);
    }

    private void handleConsumer(RabbitMQConsumer consumer) {
        consumer.handler(reply -> {
            final String body = reply.body().toString();
            try {
                final SilkCarRecordService silkCarRecordService = INJECTOR.getInstance(SilkCarRecordService.class);
                final var command = MAPPER.readValue(body, AutoDoffingSilkCarRuntimeCreateCommand.class);
                silkCarRecordService.handle(command);
                rabbitMQClient.rxBasicAck(reply.envelope().deliveryTag(), false).subscribe();
            } catch (Exception e) {
                log.error(body, e);
            }
        });
        consumer.exceptionHandler(ex -> log.error("", ex));
    }

}
