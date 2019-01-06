package com.hengyi.japp.mes.auto.apm.application;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;


public interface ApmService {
    String ADDRESS = "mes.auto.apm.queue." + ApmService.class.getName();

    void handleLog(Message<JsonObject> message);
}
