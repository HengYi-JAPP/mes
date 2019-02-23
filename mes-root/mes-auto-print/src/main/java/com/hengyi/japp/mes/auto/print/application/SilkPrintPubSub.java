package com.hengyi.japp.mes.auto.print.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.print.application.command.SilkPrintCommand;
import com.hengyi.japp.mes.auto.print.application.command.SilkPrintable;
import com.hengyi.japp.mes.auto.print.application.config.SilkPrintConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-08-14
 */
@Slf4j
@Singleton
public class SilkPrintPubSub extends JedisPubSub {
    private final SilkPrintConfig silkPrintConfig;

    @Inject
    private SilkPrintPubSub(SilkPrintConfig silkPrintConfig) {
        this.silkPrintConfig = silkPrintConfig;
    }

    @SneakyThrows
    public void onMessage(String channel, String message) {
        final JsonNode silksNode = MAPPER.readTree(message);
        final JsonNode commandNode = MAPPER.createObjectNode().set("silks", silksNode);
        final SilkPrintCommand command = MAPPER.convertValue(commandNode, SilkPrintCommand.class);
        final SilkPrintable silkPrintable = new SilkPrintable(silkPrintConfig, command);
        silkPrintable.PrintLabel();
    }

}
