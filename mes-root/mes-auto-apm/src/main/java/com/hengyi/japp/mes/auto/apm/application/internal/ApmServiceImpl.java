package com.hengyi.japp.mes.auto.apm.application.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hengyi.japp.mes.auto.apm.application.ApmService;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Singleton
public class ApmServiceImpl implements ApmService {
    private final Path logBasePath;
    private final RabbitMQClient rabbitMQClient;

    @Inject
    private ApmServiceImpl(@Named("logBasePath") Path logBasePath, RabbitMQClient rabbitMQClient) {
        this.logBasePath = logBasePath;
        this.rabbitMQClient = rabbitMQClient;
    }

    @SneakyThrows
    @Override
    public void handleLog(Message<JsonObject> message) {
        final JsonObject body = message.body();
        System.out.println(Thread.currentThread().toString() + body);
        final Path path = Paths.get("test", System.currentTimeMillis() + ".txt");
        rabbitMQClient.rxBasicAck(body.getLong("deliveryTag"), false)
                .subscribe(it -> {
                    log.debug("===");
                });
        FileUtils.write(logBasePath.resolve(path).toFile(), "123");
//        Files.write(resolve, Lists.newArrayList("123", "456"));
    }
}
