package com.hengyi.japp.mes.auto.doffing;

import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQOptions;
import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.Executors;

import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author jzb 2019-03-08
 */
public class DoffingModuleConfig {
    @Getter
    private final String persistenceUnitName;
    @Getter
    private final long fetchDelaySeconds;
    @Getter
    private final String principalName;
    @Getter
    private final long cleanDelayDays;
    @Getter
    private final String doffingExchange;
    @Getter
    private final JsonObject apiConfig;
    @Getter
    private final RabbitMQOptions rabbitMQOptions;
    private Path lineMapFilePath;
    @Getter
    private Map<String, String> lineNameMap;

    public DoffingModuleConfig(JsonNode jsonNode) {
        principalName = jsonNode.get("principalName").asText();
        persistenceUnitName = jsonNode.get("persistenceUnitName").asText();
        fetchDelaySeconds = jsonNode.get("fetchDelaySeconds").asLong();
        cleanDelayDays = jsonNode.get("cleanDelayDays").asLong();
        doffingExchange = jsonNode.get("doffingExchange").asText();
        apiConfig = new JsonObject(jsonNode.get("api").toString());
        rabbitMQOptions = new RabbitMQOptions(new JsonObject(jsonNode.get("rabbit").toString()));
        lineMapFilePath = Paths.get(jsonNode.get("rootPath").asText(), jsonNode.get("lineNameMapFile").asText());
        lineNameMap = fetchLineNameMap();
        Executors.newSingleThreadExecutor().submit(this::watchLineNameMap);
    }

    @SneakyThrows
    private Map<String, String> fetchLineNameMap() {
        return YAML_MAPPER.readValue(lineMapFilePath.toFile(), Map.class);
    }

    @SneakyThrows
    private void watchLineNameMap() {
        final WatchService watchService = FileSystems.getDefault().newWatchService();
        lineMapFilePath.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        WatchKey key;
        while ((key = watchService.take()) != null) {
            lineNameMap = fetchLineNameMap();
            key.reset();
        }
    }

}
