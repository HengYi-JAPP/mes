package com.hengyi.japp.mes.auto.report.application;

import redis.clients.jedis.Jedis;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.hengyi.japp.mes.auto.report.Report.JEDIS_POOL;

/**
 * @author jzb 2019-05-20
 */
public interface RedisService {
    String EVENT_SOURCE_KEY_PREFIX = "EventSource.";

    static <T> T call(Function<Jedis, T> function) {
        try (Jedis jedis = JEDIS_POOL.getResource()) {
            return function.apply(jedis);
        }
    }

    static void run(Consumer<Jedis> consumer) {
        try (Jedis jedis = JEDIS_POOL.getResource()) {
            consumer.accept(jedis);
        }
    }
}
