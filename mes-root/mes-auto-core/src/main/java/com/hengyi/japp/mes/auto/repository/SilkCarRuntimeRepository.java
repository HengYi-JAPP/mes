package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import java.util.Collection;

/**
 * @author jzb 2018-06-25
 */
public interface SilkCarRuntimeRepository {
    String EVENT_SOURCE_KEY_PREFIX = "EventSource.";

    static String redisKey(String code) {
        return "SilkCarRuntime[" + code + "]";
    }

    static String redisKey(SilkCar silkCar) {
        return redisKey(silkCar.getCode());
    }

    static String redisKey(SilkCarRecord silkCarRecord) {
        return redisKey(silkCarRecord.getSilkCar());
    }

    static String redisKey(SilkCarRuntime silkCarRuntime) {
        return redisKey(silkCarRuntime.getSilkCarRecord());
    }

    Single<SilkCarRuntime> create(SilkCarRecord silkCarRecord, Collection<SilkRuntime> silkRuntimes);

    Completable clearSilkCarRuntime(String code);

    Maybe<SilkCarRuntime> findByCode(String code);

    Completable addEventSource(String code, EventSource eventSource);

    default Completable addEventSource(SilkCar silkCar, EventSource eventSource) {
        return addEventSource(silkCar.getCode(), eventSource);
    }

    default Completable addEventSource(SilkCarRecord silkCarRecord, EventSource eventSource) {
        return addEventSource(silkCarRecord.getSilkCar(), eventSource);
    }

    default Completable addEventSource(SilkCarRuntime silkCarRuntime, EventSource eventSource) {
        return addEventSource(silkCarRuntime.getSilkCarRecord(), eventSource);
    }

//    Completable delete(SilkCarRuntime silkCarRuntime);
}
