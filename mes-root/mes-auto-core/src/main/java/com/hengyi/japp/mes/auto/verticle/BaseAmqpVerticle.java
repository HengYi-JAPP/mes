package com.hengyi.japp.mes.auto.verticle;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Lists;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;

import java.util.Collection;

import static com.hengyi.japp.mes.auto.Constant.AMQP.*;

/**
 * mq 初始化，定义所有 exchange
 *
 * @author jzb 2018-03-27
 */
public abstract class BaseAmqpVerticle extends AbstractVerticle {
    protected final RabbitMQClient rabbitMQClient = Jvertx.getProxy(RabbitMQClient.class);
    protected Collection<MessageConsumer> consumers = Lists.newArrayList();

    protected Completable ensureExchange() {
        final Completable exchanges$ = Completable.mergeArray(
                rabbitMQClient.rxExchangeDeclare(MES_AUTO_DYEING_RESULT_CREATE_EXCHANGE, "fanout", true, false),
                rabbitMQClient.rxExchangeDeclare(MES_AUTO_PRINT_EXCHANGE, "fanout", false, false),
                rabbitMQClient.rxExchangeDeclare(MES_AUTO_APM_EXCHANGE, "fanout", true, false),
                rabbitMQClient.rxExchangeDeclare(MES_AUTO_ES_EXCHANGE, "fanout", true, false)
        );
        return rabbitMQClient.rxStart().andThen(exchanges$);
    }

    protected <T> Completable consumer(final String queueName, final String exchangeName, final String address, Handler<Message<T>> handler) {
        return consumer(queueName, exchangeName, "", address, handler);
    }

    protected <T> Completable consumer(final String queueName, final String exchangeName, final String routingKey, final String address, Handler<Message<T>> handler) {
        return rabbitMQClient.rxQueueDeclare(queueName, true, false, false)
                .flatMapCompletable(it -> rabbitMQClient.rxQueueBind(queueName, exchangeName, routingKey))
                .andThen(rabbitMQClient.rxBasicConsume(queueName, address, false)).ignoreElement()
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
