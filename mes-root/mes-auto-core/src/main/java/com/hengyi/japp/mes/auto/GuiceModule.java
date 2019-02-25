package com.hengyi.japp.mes.auto;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * @author jzb 2018-03-21
 */
public class GuiceModule extends AbstractModule {
    protected final Vertx vertx;

    public GuiceModule(Vertx vertx) {
        this.vertx = vertx;
    }

    @Provides
    @Singleton
    private MesAutoConfig mesAutoConfig() {
        return new MesAutoConfig();
    }

    @Provides
    @Singleton
    private RabbitMQClient RabbitMQClient(MesAutoConfig config) {
        return RabbitMQClient.create(vertx, config.getRabbitMQOptions());
    }

    @Provides
    @Singleton
    private JWTAuth JWTAuth(MesAutoConfig config) {
        return JWTAuth.create(vertx, config.getJwtAuthOptions());
    }

    @Provides
    @Singleton
    private KeyStore keystore(MesAutoConfig config) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        final KeyStore keystore = KeyStore.getInstance("jceks");
        final File file = config.getRootPath().resolve("mes.jceks").toFile();
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
