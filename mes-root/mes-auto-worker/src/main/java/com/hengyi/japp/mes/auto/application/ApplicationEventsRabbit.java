package com.hengyi.japp.mes.auto.application;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.domain.ExceptionRecord;
import com.hengyi.japp.mes.auto.domain.LineMachineProductPlan;
import com.hengyi.japp.mes.auto.domain.Notification;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import com.hengyi.japp.mes.auto.interfaces.jikon.dto.GetSilkSpindleInfoDTO;
import com.hengyi.japp.mes.auto.interfaces.riamb.dto.RiambFetchSilkCarRecordResultDTO;
import com.hengyi.japp.mes.auto.repository.ExceptionRecordRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import io.vertx.reactivex.core.Vertx;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class ApplicationEventsRabbit implements ApplicationEvents {
    private final Vertx vertx;
    private final OperatorRepository operatorRepository;
    private final ExceptionRecordRepository exceptionRecordRepository;

    @Inject
    private ApplicationEventsRabbit(Vertx vertx, OperatorRepository operatorRepository, ExceptionRecordRepository exceptionRecordRepository) {
        this.vertx = vertx;
        this.operatorRepository = operatorRepository;
        this.exceptionRecordRepository = exceptionRecordRepository;
    }

    @SneakyThrows
    @Override
    public void refreshAbnormalBoard() {
        vertx.eventBus().publish("mes-auto://websocket/boards/abnormal/refresh", null);
    }

    @SneakyThrows
    @Override
    public void fire(LineMachineProductPlan lineMachineProductPlan) {
        final String message = MAPPER.writeValueAsString(lineMachineProductPlan);
        vertx.eventBus().publish("mes-auto://websocket/boards/abnormal/productPlan", message);
    }

    @SneakyThrows
    @Override
    public void fire(ExceptionRecord exceptionRecord) {
        final String message = MAPPER.writeValueAsString(exceptionRecord);
        vertx.eventBus().publish("mes-auto://websocket/boards/abnormal/exceptionRecord", message);
    }

    @SneakyThrows
    @Override
    public void fire(Notification notification) {
        final String message = MAPPER.writeValueAsString(notification);
        vertx.eventBus().publish("mes-auto://websocket/boards/abnormal/notification", message);
    }

    @SneakyThrows
    @Override
    public void refreshSilkCarRuntimeReportBoard() {
        vertx.eventBus().publish("mes-auto://websocket/boards/silkCarRuntimeReport/refresh", null);
    }

    @SneakyThrows
    @Override
    public void fire(String silkCarCode, EventSource eventSource) {
        final Map<String, Object> map = ImmutableMap.of("silkCarCode", silkCarCode, "eventSource", eventSource);
        final String message = MAPPER.writeValueAsString(map);
        vertx.eventBus().publish("mes-auto://websocket/boards/silkCarRuntimeReport/events", message);
    }

    @SneakyThrows
    @Override
    public void fire(SilkCarRuntime silkCarRuntime, GetSilkSpindleInfoDTO dto, List<String> reasons) {
        final Map<String, Object> map = ImmutableMap.of("silkCarRuntime", silkCarRuntime, "dto", dto, "reasons", reasons);
        final String message = MAPPER.writeValueAsString(map);
        vertx.eventBus().publish("mes-auto://websocket/boards/JikonAdapterSilkCarInfoFetchReasons", message);
    }

    @SneakyThrows
    @Override
    public void fire(Principal principal, SilkCarRuntime silkCarRuntime, RiambFetchSilkCarRecordResultDTO dto, List<String> reasons) {
        final Map<String, Object> map = ImmutableMap.of("principalName", principal.getName(), "silkCarRuntime", silkCarRuntime, "dto", dto, "reasons", reasons);
        final String message = MAPPER.writeValueAsString(map);
        vertx.eventBus().publish("mes-auto://websocket/boards/RiambSilkCarInfoFetchReasons", message);
    }

}
