package com.hengyi.japp.mes.auto.doffing;

import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQOptions;
import lombok.Getter;

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

    public DoffingModuleConfig(JsonNode jsonNode) {
        principalName = jsonNode.get("principalName").asText();
        persistenceUnitName = jsonNode.get("persistenceUnitName").asText();
        fetchDelaySeconds = jsonNode.get("fetchDelaySeconds").asLong();
        cleanDelayDays = jsonNode.get("cleanDelayDays").asLong();
        doffingExchange = jsonNode.get("doffingExchange").asText();
        apiConfig = new JsonObject(jsonNode.get("api").toString());
        rabbitMQOptions = new RabbitMQOptions(new JsonObject(jsonNode.get("rabbit").toString()));
    }

}
