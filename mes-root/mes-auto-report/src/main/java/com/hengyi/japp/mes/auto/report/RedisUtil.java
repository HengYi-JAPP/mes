package com.hengyi.japp.mes.auto.report;

import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import io.vertx.redis.RedisOptions;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
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
    final static JedisPool jedisPool = buildJedisPool();
    public static String EVENT_SOURCE_KEY_PREFIX = "EventSource.";

    private static JedisPool buildJedisPool() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        final RedisOptions redisOptions = INJECTOR.getInstance(MesAutoConfig.class).getRedisOptions();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        final JedisPool jedisPool = new JedisPool(poolConfig, redisOptions.getHost(), 6379);
        return jedisPool;
    }

    public static Map<String, String> getRedis(String code) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll("SilkCarRuntime[" + code + "]");
        }
    }

    public static Stream<String> getAllSilkCarRecords() {
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> keys = jedis.keys("SilkCarRuntime*");
            return keys.stream().map(key -> jedis.hget(key, "silkCarRecord"));
        }
    }

    public static Stream<Map<String, String>> getALlSilkCarRecordsEvents() {
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> keys = jedis.keys("SilkCarRuntime*");
            return keys.stream().map(key -> jedis.hgetAll(key));
        }
    }
}
