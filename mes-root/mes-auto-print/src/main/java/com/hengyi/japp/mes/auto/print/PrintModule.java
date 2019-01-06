package com.hengyi.japp.mes.auto.print;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hengyi.japp.mes.auto.print.application.SilkPrintService;
import com.hengyi.japp.mes.auto.print.application.config.SilkPrintConfig;
import com.hengyi.japp.mes.auto.print.application.internal.SilkPrintServiceImpl;
import io.vertx.core.impl.Utils;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static io.vertx.config.yaml.YamlProcessor.YAML_MAPPER;

/**
 * @author jzb 2018-08-06
 */
public class PrintModule extends AbstractModule {
    private final Vertx vertx;

    public PrintModule(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    protected void configure() {
        bind(SilkPrintService.class).to(SilkPrintServiceImpl.class);
    }

    @SneakyThrows
    @Provides
    @Singleton
    @Named("rootConfig")
    private JsonObject config() {
        final Path configPath;
        if (Utils.isWindows()) {
            configPath = Paths.get("d:/mes/config.yml");
        } else {
            configPath = Paths.get("/home/mes/config.yml");
        }
        final Map map = YAML_MAPPER.readValue(configPath.toFile(), Map.class);
        return new JsonObject(map);
    }

    @Provides
    @Singleton
    @Named("rootPath")
    private Path rootPath(@Named("rootConfig") JsonObject rootConfig) {
        final String rootPath = rootConfig.getString("rootPath", "/home/mes");
        return Paths.get(rootPath);
    }

    @Provides
    @Singleton
    @Named("autoPrintRootPath")
    private Path autoPrintRootPath(@Named("rootPath") Path rootPath) {
        return rootPath.resolve("auto-print");
    }

    @SneakyThrows
    @Provides
    private SilkPrintConfig SilkPrintConfig(@Named("autoPrintRootPath") Path autoPrintRootPath) {
        final Path configPath = autoPrintRootPath.resolve("silkPrinter.config.yml");
        final SilkPrintConfig config = YAML_MAPPER.readValue(configPath.toFile(), SilkPrintConfig.class);
        return config;
    }

    @SneakyThrows
    @Provides
    @Singleton
    private RabbitMQClient RabbitMQClient(@Named("rootPath") Path rootPath) {
        final Path configPath = rootPath.resolve("rabbitMQ.config.yml");
        final Map map = YAML_MAPPER.readValue(configPath.toFile(), Map.class);
        final JsonObject config = new JsonObject(map);
        final RabbitMQOptions options = new RabbitMQOptions(config);
        return RabbitMQClient.create(vertx, options);
    }

    @SneakyThrows
    @Provides
    @Named("http")
    private JsonObject http() {
        return new JsonObject();
    }

    @Provides
    @Named("http.port")
    private int httpPort(@Named("http") JsonObject http) {
        return http.getInteger("port", 9000);
    }

    @Provides
    private Vertx vertx() {
        return vertx;
    }

}
