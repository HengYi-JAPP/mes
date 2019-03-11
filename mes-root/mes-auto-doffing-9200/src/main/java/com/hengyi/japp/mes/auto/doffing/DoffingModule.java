package com.hengyi.japp.mes.auto.doffing;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hengyi.japp.mes.auto.doffing.application.DoffingService;
import com.hengyi.japp.mes.auto.doffing.application.intenal.DoffingServiceImpl;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import lombok.SneakyThrows;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;

/**
 * @author jzb 2019-03-07
 */
public class DoffingModule extends AbstractModule {
    private final Vertx vertx;

    public DoffingModule(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    protected void configure() {
        bind(DoffingService.class).to(DoffingServiceImpl.class);
    }

    @Provides
    @Singleton
    @Named("rootPath")
    private Path rootPath() {
        return Paths.get(System.getProperty("mes.auto.doffing.path", "/home/mes/auto-doffing"));
    }

    @SneakyThrows
    @Provides
    @Singleton
    private DoffingModuleConfig MesAutoDoffingModuleConfig(@Named("rootPath") Path rootPath) {
        final JsonNode jsonNode = YAML_MAPPER.readTree(rootPath.resolve("config.yml").toFile());
        return new DoffingModuleConfig(jsonNode);
    }

    @Provides
    @Singleton
    private RabbitMQClient RabbitMQClient(DoffingModuleConfig config) {
        return RabbitMQClient.create(vertx, config.getRabbitMQOptions());
    }

    @Provides
    @Singleton
    private EntityManagerFactory EntityManagerFactory(DoffingModuleConfig config) {
        return Persistence.createEntityManagerFactory(config.getPersistenceUnitName());
    }

    @Provides
    @Singleton
    private EntityManager EntityManager(EntityManagerFactory emf) {
        return emf.createEntityManager();
    }
}
