package com.hengyi.japp.mes.auto.config;

import io.vertx.core.json.JsonObject;
import lombok.Data;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * @author jzb 2019-02-23
 */
@Data
public class CorsConfig {
    private final Set<String> domainPatterns;

    public CorsConfig(JsonObject cors) {
        domainPatterns = cors.getJsonArray("domainPatterns").stream().map(it -> (String) it).collect(toSet());
    }
}
