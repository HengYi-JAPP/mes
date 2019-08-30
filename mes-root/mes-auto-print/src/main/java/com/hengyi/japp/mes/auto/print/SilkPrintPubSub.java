package com.hengyi.japp.mes.auto.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.print.command.SilkPrintCommand;
import com.hengyi.japp.mes.auto.print.config.PrinterConfig;
import com.hengyi.japp.mes.auto.print.printable.SilkPrintable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-08-14
 */
@Slf4j
@Singleton
public class SilkPrintPubSub extends JedisPubSub {
    public static final String CHANNEL_PREFIX = "SilkBarcodePrinter";
    @Getter
    private final String CHANNEL;
    private final PrinterConfig printerConfig;

    @Inject
    private SilkPrintPubSub(PrinterConfig printerConfig) {
        this.printerConfig = printerConfig;
        CHANNEL = String.join("-", CHANNEL_PREFIX, printerConfig.getId(), printerConfig.getName());
    }

    public void onMessage(String channel, String message) {
        if (!CHANNEL.equals(channel)) {
            log.error("channel[" + channel + "]：" + message);
            return;
        }
        try {
            final JsonNode silksNode = MAPPER.readTree(message);
            final JsonNode commandNode = MAPPER.createObjectNode().set("silks", silksNode);
            final SilkPrintCommand command = MAPPER.convertValue(commandNode, SilkPrintCommand.class);
            final SilkPrintable silkPrintable = new SilkPrintable(command);
            silkPrintable.PrintLabel();
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        if (subscribedChannels == 1 && CHANNEL.equals(channel)) {
            log.info("打印机" + "[" + printerConfig.getId() + "]: " + printerConfig.getName() + " 注册成功！");
            return;
        }
        throw new RuntimeException("打印机注册不成功，请检查配置！");
    }
}
