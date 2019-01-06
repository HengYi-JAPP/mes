package com.hengyi.japp.mes.auto.print.application.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.print.application.SilkPrintService;
import com.hengyi.japp.mes.auto.print.application.command.SilkPrintCommand;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-08-14
 */
@Slf4j
@Singleton
public class SilkPrintServiceImpl implements SilkPrintService {
    private final RabbitMQClient rabbitMQClient;

    @Inject
    private SilkPrintServiceImpl(RabbitMQClient rabbitMQClient) {
        this.rabbitMQClient = rabbitMQClient;
    }

    @SneakyThrows
    @Override
    public void handleSilkPrint(Message<JsonObject> msg) {
        final JsonObject json = msg.body();
        final JsonNode silksNode = MAPPER.readTree(json.getString("body"));
        final JsonNode commandNode = MAPPER.createObjectNode().set("silks", silksNode);
        final SilkPrintCommand command = MAPPER.convertValue(commandNode, SilkPrintCommand.class);
        command.toPrintable().PrintLabel();
        rabbitMQClient.rxBasicAck(json.getLong("deliveryTag"), false).subscribe();
    }
}
