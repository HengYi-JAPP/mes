package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.event.EventSource;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;

/**
 * @author jzb 2018-07-10
 */
public interface EventSourceRepository {

    Single<? extends EventSource> find(String filePath);

    <T extends EventSource> Single<T> save(T eventSource);

    void handlePersistence(Message<JsonObject> message);
}
