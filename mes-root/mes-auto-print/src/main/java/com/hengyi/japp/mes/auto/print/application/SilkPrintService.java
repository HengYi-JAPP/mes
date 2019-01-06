package com.hengyi.japp.mes.auto.print.application;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;

/**
 * @author jzb 2018-08-14
 */
public interface SilkPrintService {
    void handleSilkPrint(Message<JsonObject> msg);
}
