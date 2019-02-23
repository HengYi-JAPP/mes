package com.hengyi.japp.mes.auto;

import com.google.inject.Singleton;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.redis.RedisOptions;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author jzb 2019-02-23
 */
@Singleton
public class MesAutoConfig {
    @Getter
    private final Path rootPath;
    private final JsonObject rootConfig;
    @Getter
    private final JsonObject pdaConfig;
    @Getter
    private final JsonObject openConfig;
    @Getter
    private final JsonObject mongoOptions;
    @Getter
    private final RedisOptions redisOptions;
    @Getter
    private final RabbitMQOptions rabbitMQOptions;
    @Getter
    private final JWTAuthOptions jwtAuthOptions;

    @Getter
    private final JsonObject jikonDsOptions;

    MesAutoConfig() {
        rootPath = Paths.get(System.getProperty("japp.mes.auto.path", "/home/mes/auto"));
        rootConfig = Util.readJsonObject(rootPath.resolve("config.yml"));
        pdaConfig = rootConfig.getJsonObject("pda");
        openConfig = rootConfig.getJsonObject("open");
        mongoOptions = rootConfig.getJsonObject("mongo");
        redisOptions = new RedisOptions(rootConfig.getJsonObject("redis"));
        rabbitMQOptions = new RabbitMQOptions(rootConfig.getJsonObject("rabbit"));
        final PubSecKeyOptions pubSecKey = new PubSecKeyOptions(rootConfig.getJsonObject("jwt"));
        jwtAuthOptions = new JWTAuthOptions().addPubSecKey(pubSecKey);

        jikonDsOptions = rootConfig.getJsonObject("jikon_ds");
    }

    public Path apkPath() {
        return rootPath.resolve("apk");
    }

    public JsonObject apkInfo() {
        final Path path = Paths.get("latest", "info.yml");
        return Util.readJsonObject(apkPath().resolve(path));
    }

    public String apkFileName(String version) {
        if (version.equalsIgnoreCase("latest")) {
            final JsonObject apkInfo = apkInfo();
            version = apkInfo.getString("version");
        }
        return apkPath().resolve(Paths.get(version, "mes-auto.apk")).toFile().getPath();
    }

    public Path luceneIndexPath(Class<?> clazz) {
        final Path path = Paths.get("db", "lucene", clazz.getSimpleName());
        return rootPath.resolve(path);
    }

    public Path luceneTaxoPath(Class<?> clazz) {
        final Path path = Paths.get("db", "lucene", clazz.getSimpleName() + "_Taxonomy");
        return rootPath.resolve(path);
    }
}
