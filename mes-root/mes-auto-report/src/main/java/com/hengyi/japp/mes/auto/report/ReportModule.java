package com.hengyi.japp.mes.auto.report;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mongodb.MongoCredential.createCredential;

/**
 * @author jzb 2019-05-20
 */
public class ReportModule extends AbstractModule {

    @Provides
    @Singleton
    private MongoClient MongoClient(MesAutoConfig mesAutoConfig) {
        final JsonObject mongoOptions = mesAutoConfig.getMongoOptions();
        final MongoClientSettings.Builder builder = MongoClientSettings.builder().applyConnectionString(connectionString(mongoOptions));
        final String username = mongoOptions.getString("username");
        if (J.nonBlank(username)) {
            final char[] password = mongoOptions.getString("password").toCharArray();
            final String authSource = mongoOptions.getString("authSource", "admin");
            builder.credential(createCredential(username, authSource, password));
        }
        return MongoClients.create(builder.build());
    }

    private ConnectionString connectionString(JsonObject mongoOptions) {
        final String host = mongoOptions.getString("host");
        final Integer port = mongoOptions.getInteger("port", 27017);
        final Collection<Pair<String, ?>> objects = Sets.newHashSet();
        Optional.ofNullable(mongoOptions.getInteger("maxPoolSize"))
                .filter(Objects::nonNull)
                .map(it -> Pair.of("maxpoolsize", it))
                .ifPresent(objects::add);
        Optional.ofNullable(mongoOptions.getInteger("minPoolSize"))
                .filter(Objects::nonNull)
                .map(it -> Pair.of("minpoolsize", it))
                .ifPresent(objects::add);
        Optional.ofNullable(mongoOptions.getInteger("maxIdleTimeMS"))
                .filter(Objects::nonNull)
                .map(it -> Pair.of("maxidletimems", it))
                .ifPresent(objects::add);
        Optional.ofNullable(mongoOptions.getInteger("maxLifeTimeMS"))
                .filter(Objects::nonNull)
                .map(it -> Pair.of("maxlifetimems", it))
                .ifPresent(objects::add);
        Optional.ofNullable(mongoOptions.getInteger("waitQueueMultiple"))
                .filter(Objects::nonNull)
                .map(it -> Pair.of("waitqueuemultiple", it))
                .ifPresent(objects::add);
        Optional.ofNullable(mongoOptions.getInteger("waitQueueTimeoutMS"))
                .filter(Objects::nonNull)
                .map(it -> Pair.of("waitqueuetimeoutms", it))
                .ifPresent(objects::add);
        final String collect = J.isEmpty(objects) ? ""
                : "/?" + objects.parallelStream()
                .map(it -> it.getLeft() + "=" + it.getRight())
                .collect(Collectors.joining("&"));
        return new ConnectionString("mongodb://" + host + ":" + port + collect);
    }

    @Provides
    private MongoDatabase MongoDatabase(MesAutoConfig mesAutoConfig, MongoClient mongoClient) {
        final JsonObject mongoOptions = mesAutoConfig.getMongoOptions();
        return mongoClient.getDatabase(mongoOptions.getString("db_name", "mes-auto"));
    }

}
