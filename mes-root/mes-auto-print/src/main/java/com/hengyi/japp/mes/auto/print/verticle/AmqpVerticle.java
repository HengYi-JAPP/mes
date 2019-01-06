package com.hengyi.japp.mes.auto.print.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.print.application.SilkPrintService;
import com.hengyi.japp.mes.auto.print.application.config.SilkPrintConfig;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;

import java.util.Collection;

/**
 * @author jzb 2018-04-18
 */
public class AmqpVerticle extends AbstractVerticle {
    public static final String MES_AUTO_SILK_PRINT_PREFIX = "Silk";
    public static final String MES_AUTO_PRINT_EXCHANGE = "mes.auto.print.exchange";
    protected final RabbitMQClient rabbitMQClient = Jvertx.getProxy(RabbitMQClient.class);
    protected Collection<MessageConsumer> consumers = Lists.newArrayList();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        final Completable completable = Completable.mergeArray(
                silkBarcodePrint()
        );
        ensureExchange().andThen(completable).subscribe(startFuture::complete, startFuture::fail);
    }

    protected Completable ensureExchange() {
        final Completable exchanges$ = Completable.mergeArray(
                rabbitMQClient.rxExchangeDeclare(MES_AUTO_PRINT_EXCHANGE, "fanout", false, false)
        );
        return rabbitMQClient.rxStart().andThen(exchanges$);
    }

    private Completable silkBarcodePrint() {
        final SilkPrintConfig silkPrintConfig = Jvertx.getProxy(SilkPrintConfig.class);
        if (silkPrintConfig == null) {
            return Completable.complete();
        }
        final String routingKey = MES_AUTO_SILK_PRINT_PREFIX + "." + silkPrintConfig.getPrinterConfig().getId();
        final SilkPrintService service = Jvertx.getProxy(SilkPrintService.class);
        return consumer(routingKey, MES_AUTO_PRINT_EXCHANGE, routingKey, routingKey, service::handleSilkPrint);
    }

    protected <T> Completable consumer(final String queueName, final String exchangeName, final String routingKey, final String address, Handler<Message<T>> handler) {
        return rabbitMQClient.rxQueueDeclare(queueName, false, false, false)
                .flatMapCompletable(it -> rabbitMQClient.rxQueueBind(queueName, exchangeName, routingKey))
                .andThen(rabbitMQClient.rxBasicConsume(queueName, address, false))
                .doOnComplete(() -> {
                    final MessageConsumer<T> consumer = vertx.eventBus().consumer(address, handler);
                    consumers.add(consumer);
                });
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        Flowable.fromIterable(consumers)
                .flatMapCompletable(MessageConsumer::rxUnregister)
                .andThen(rabbitMQClient.rxStop())
                .subscribe(stopFuture::complete, stopFuture::fail);
    }

}
