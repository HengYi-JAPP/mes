package com.hengyi.japp.mes.auto.application;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.interfaces.jikon.dto.GetSilkSpindleInfoDTO;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import lombok.SneakyThrows;

import java.util.List;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-06-25
 */
@Singleton
public class ApplicationEventsRabbit implements ApplicationEvents {
    private final Vertx vertx;
    private final RabbitMQClient rabbitMQClient;

    @Inject
    private ApplicationEventsRabbit(Vertx vertx, RabbitMQClient rabbitMQClient) {
        this.vertx = vertx;
        this.rabbitMQClient = rabbitMQClient;
    }

    @SneakyThrows
    @Override
    public void fire(LineMachineProductPlan lineMachineProductPlan) {
        final LineMachine lineMachine = lineMachineProductPlan.getLineMachine();
        final Line line = lineMachine.getLine();
        final ImmutableMap<String, Object> map = ImmutableMap.of("lineMachine", lineMachine, "batch", lineMachineProductPlan.getBatch());
        final String message = MAPPER.writeValueAsString(map);
        vertx.eventBus().publish("mes-auto://websocket/boards/workshopExceptionReport/lines/" + line.getId(), message);
    }

    @SneakyThrows
    @Override
    public void fire(SilkCarRuntime silkCarRuntime, GetSilkSpindleInfoDTO dto, List<String> reasons) {
        final ImmutableMap<String, Object> map = ImmutableMap.of("silkCarRuntime", silkCarRuntime, "reasons", reasons);
        final String message = MAPPER.writeValueAsString(map);
        vertx.eventBus().publish("mes-auto://websocket/boards/JikonAdapterSilkCarInfoFetchReasons", message);
    }

    @SneakyThrows
    @Override
    public void fire(Silk silk, Operator operator) {
        final LineMachine lineMachine = silk.getLineMachine();
        final Line line = lineMachine.getLine();
        final ImmutableMap<String, Object> map = ImmutableMap.of("silk", silk, "operator", operator);
        final String message = MAPPER.writeValueAsString(map);
        vertx.eventBus().publish("mes-auto://websocket/boards/workshopSilkExceptionReport/lines/" + line.getId(), message);
    }
}
