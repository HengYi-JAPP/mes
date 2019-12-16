package com.hengyi.japp.mes.auto.report;

import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;

/**
 * @author liuyuan
 * @create 2019-06-04 13:53
 * @description
 **/
public class RedisUtil {

    public static Map<String, String> getRedis(String code) {
        try (Jedis jedis = INJECTOR.getInstance(Jedis.class)) {
            return jedis.hgetAll("SilkCarRuntime[" + code + "]");
        }
    }

    public static Stream<String> getAllSilkCarRecords() {
        try (Jedis jedis = INJECTOR.getInstance(Jedis.class)) {
            Set<String> keys = jedis.keys("SilkCarRuntime*");
            return keys.stream().map(key -> jedis.hget(key, "silkCarRecord"));
        }
    }

    public static Stream<Map<String, String>> getALlSilkCarRecordsEvents() {
        try (Jedis jedis = INJECTOR.getInstance(Jedis.class)) {
            Set<String> keys = jedis.keys("SilkCarRuntime*");
            return keys.stream().map(key -> jedis.hgetAll(key));
        }
    }
}
