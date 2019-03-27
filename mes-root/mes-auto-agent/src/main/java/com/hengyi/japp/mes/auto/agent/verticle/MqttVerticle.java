package com.hengyi.japp.mes.auto.agent.verticle;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.mqtt.MqttEndpoint;
import io.vertx.reactivex.mqtt.MqttServer;
import io.vertx.reactivex.mqtt.MqttTopicSubscription;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jzb 2019-03-27
 */
public class MqttVerticle extends AbstractVerticle {
    @Override
    public Completable rxStart() {
        MqttServer mqttServer = MqttServer.create(vertx);
        return mqttServer.endpointHandler(endpoint -> {
            handle(endpoint);
            endpoint.accept(false);
        }).rxListen(1883).ignoreElement();
    }

    private void handle(MqttEndpoint endpoint) {
        endpoint.publishHandler(message -> {
            System.out.println("Just received message on [" + message.topicName() + "] payload [" +
                    message.payload() + "] with QoS [" +
                    message.qosLevel() + "]");
        });
        endpoint.subscribeHandler(subscribe -> {
            List<MqttQoS> grantedQosLevels = new ArrayList<>();
            for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
                System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
                grantedQosLevels.add(s.qualityOfService());
            }
            // ack the subscriptions request
            endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);
        });
        endpoint.unsubscribeHandler(unsubscribe -> {
            for (String t : unsubscribe.topics()) {
                System.out.println("Unsubscription for " + t);
            }
            // ack the subscriptions request
            endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
        });
        endpoint.disconnectHandler(v -> {
            System.out.println("Received disconnect from client");
        });
        endpoint.pingHandler(v -> {
            System.out.println("Ping received from client");
        });
    }
}
