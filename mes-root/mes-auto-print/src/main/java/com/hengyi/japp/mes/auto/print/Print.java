package com.hengyi.japp.mes.auto.print;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author jzb 2018-08-06
 */
@Slf4j
public class Print {
    public static final Injector INJECTOR = Guice.createInjector(new PrintModule());

    public static void start(String[] args) {
        final JedisPool jedisPool = INJECTOR.getInstance(JedisPool.class);
        try (Jedis jedis = jedisPool.getResource()) {
            final SilkPrintPubSub silkPrintPubSub = INJECTOR.getInstance(SilkPrintPubSub.class);
            jedis.subscribe(silkPrintPubSub, silkPrintPubSub.getCHANNEL());
        }
    }

    public static void stop(String[] args) {
        System.exit(0);
    }

    public static void main(String[] args) {
        Print.start(null);
    }

}
