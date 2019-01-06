package com.hengyi.japp.mes.auto.apm;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.apm.application.ApmService;
import com.hengyi.japp.mes.auto.apm.application.internal.ApmServiceImpl;
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
public class ApmModule extends GuiceModule {
    public ApmModule(Vertx vertx) {
        super(vertx);
    }

    @Override
    protected void configure() {
        bind(ApmService.class).to(ApmServiceImpl.class);
    }

    @Provides
    @Singleton
    @Named("logBasePath")
    private Path logBasePath(@Named("autoRootPath") Path autoRootPath) {
        final Path path = Paths.get("apm", "log");
        return autoRootPath.resolve(path);
    }

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
}
