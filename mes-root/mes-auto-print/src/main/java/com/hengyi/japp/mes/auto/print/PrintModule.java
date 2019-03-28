package com.hengyi.japp.mes.auto.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hengyi.japp.mes.auto.print.config.PrinterConfig;
import com.hengyi.japp.mes.auto.print.config.SilkPrintConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;

/**
 * @author jzb 2018-08-06
 */
public class PrintModule extends AbstractModule {

    @Provides
    @Singleton
    @Named("rootPath")
    private Path rootPath() {
        return Paths.get(System.getProperty("mes.auto.print.path", "/home/mes/auto-print"));
    }

    @Provides
    @Singleton
    @Named("rootConfig")
    private JsonNode config(@Named("rootPath") Path rootPath) throws IOException {
        return YAML_MAPPER.readTree(rootPath.resolve("config.yml").toFile());
    }

    @Provides
    @Singleton
    private PrinterConfig printerConfig(@Named("rootConfig") JsonNode config) {
        return MAPPER.convertValue(config.get("printerConfig"), PrinterConfig.class);
    }

    @Provides
    @Singleton
    private SilkPrintConfig silkPrintConfig(@Named("rootConfig") JsonNode config) {
        return MAPPER.convertValue(config.get("silkPrintConfig"), SilkPrintConfig.class);
    }

    @Provides
    @Singleton
    private JedisPool jedisPool(@Named("rootConfig") JsonNode config) {
        final JsonNode redis = config.get("redis");
        return new JedisPool(new JedisPoolConfig(), redis.get("host").asText());
    }

}
