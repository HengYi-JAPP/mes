package com.hengyi.japp.mes.auto.report.application;

import redis.clients.jedis.Jedis;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.hengyi.japp.mes.auto.report.Report.JEDIS_POOL;

/**
 * @author jzb 2019-05-20
 */
public interface RedisService {
    String EVENT_SOURCE_KEY_PREFIX = "EventSource.";
    Pattern SILK_CAR_CODE_PATTERN = Pattern.compile("^SilkCarRuntime\\[(\\w+)\\]$");

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

    static Stream<String> listSilkCarRuntimeSilkCarCodes() {
        final Set<String> keys = RedisService.call(jedis -> jedis.keys("SilkCarRuntime\\[*"));
        return keys.parallelStream().map(key -> {
            final Matcher matcher = SILK_CAR_CODE_PATTERN.matcher(key);
            final boolean find = matcher.find();
            if (find) {
                return matcher.group(1);
            }
            return null;
        }).filter(Objects::nonNull);
    }
}
