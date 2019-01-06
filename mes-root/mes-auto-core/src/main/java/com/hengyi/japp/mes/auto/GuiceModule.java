package com.hengyi.japp.mes.auto;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * @author jzb 2018-03-21
 */
public class GuiceModule extends AbstractModule {
    protected final Vertx vertx;

    protected GuiceModule(Vertx vertx) {
        this.vertx = vertx;
    }

    @Provides
    @Singleton
    @Named("rootConfig")
    private JsonObject config() {
        final Path configPath = Paths.get("/home/mes/config.yml");
        return Util.readJsonObject(configPath);
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
    @Named("autoRootPath")
    private Path autoRootPath(@Named("rootPath") Path rootPath) {
        return rootPath.resolve("auto");
    }

    @Provides
    @Singleton
    private RabbitMQClient RabbitMQClient(@Named("rootPath") Path rootPath) {
        final Path configPath = rootPath.resolve("rabbitMQ.config.yml");
        final JsonObject config = Util.readJsonObject(configPath);
        final RabbitMQOptions options = new RabbitMQOptions(config);
        return RabbitMQClient.create(vertx, options);
    }

    @Provides
    @Singleton
    private JWTAuth JWTAuth(Vertx vertx, @Named("rootPath") Path rootPath) {
        final Path configPath = rootPath.resolve("jwt.config.yml");
        final JsonObject config = Util.readJsonObject(configPath);
        final PubSecKeyOptions pubSecKey = new PubSecKeyOptions(config);
        final JWTAuthOptions options = new JWTAuthOptions().addPubSecKey(pubSecKey);
        return JWTAuth.create(vertx, options);
    }

    @Provides
    @Singleton
    private KeyStore keystore(@Named("rootPath") Path rootPath) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        final KeyStore keystore = KeyStore.getInstance("jceks");
        final File file = rootPath.resolve("mes.jceks").toFile();
        keystore.load(new FileInputStream(file), "esb-open-tomking".toCharArray());
        return keystore;
    }

    @Provides
    @Singleton
    private PrivateKey PrivateKey(KeyStore keyStore) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return (PrivateKey) keyStore.getKey("esb-open", "esb-open-tomking".toCharArray());
    }

    @Provides
    private Vertx vertx() {
        return vertx;
    }

}
