package com.hengyi.japp.mes.auto.agent;

import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.hengyi.japp.mes.auto.GuiceModule;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static io.vertx.config.yaml.YamlProcessor.YAML_MAPPER;

/**
 * @author jzb 2018-03-21
 */
public class AgentModule extends GuiceModule {
    public AgentModule(Vertx vertx) {
        super(vertx);
    }

    //========== web =====================
    @SneakyThrows
    @Provides
    @Named("http")
    private JsonObject http(@Named("autoRootPath") Path autoRootPath) {
        final Path configPath = autoRootPath.resolve("http.config.yml");
        final Map map = YAML_MAPPER.readValue(configPath.toFile(), Map.class);
        return new JsonObject(map);
    }

    @Provides
    @Named("http.port")
    private int httpPort(@Named("http") JsonObject http) {
        return http.getInteger("port", 8080);
    }

    //========== pda =====================
    @SneakyThrows
    @Provides
    @Named("apkInfo")
    private JsonObject pdaVersion(@Named("autoRootPath") Path autoRootPath) {
        final Path configPath = autoRootPath.resolve(Paths.get("apk", "latest", "info.yml"));
        final Map map = YAML_MAPPER.readValue(configPath.toFile(), Map.class);
        return new JsonObject(map);
    }

    @SneakyThrows
    @Provides
    @Named("pda")
    private JsonObject pda(@Named("autoRootPath") Path autoRootPath) {
        final Path configPath = autoRootPath.resolve("pda.config.yml");
        final Map map = YAML_MAPPER.readValue(configPath.toFile(), Map.class);
        return new JsonObject(map);
    }

    @Provides
    @Named("pda.port")
    private int pdaPort(@Named("pda") JsonObject http) {
        return http.getInteger("port", 9998);
    }

    //========== open =====================
    @SneakyThrows
    @Provides
    @Named("open")
    private JsonObject open(@Named("autoRootPath") Path autoRootPath) {
        final Path configPath = autoRootPath.resolve("open.config.yml");
        final Map map = YAML_MAPPER.readValue(configPath.toFile(), Map.class);
        return new JsonObject(map);
    }

    @Provides
    @Named("open.port")
    private int openPort(@Named("open") JsonObject open) {
        return open.getInteger("port", 9999);
    }

}
