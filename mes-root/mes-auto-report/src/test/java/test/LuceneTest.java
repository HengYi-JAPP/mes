package test;

import com.github.ixtf.japp.core.J;
import com.google.inject.Guice;
import com.hengyi.japp.mes.auto.GuiceModule;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.report.ReportModule;
import com.hengyi.japp.mes.auto.report.application.dto.stripping.StrippingReport;
import io.vertx.reactivex.core.Vertx;
import io.vertx.redis.RedisOptions;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import test.AAReport_3000.Workshops;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static com.hengyi.japp.mes.auto.report.Report.JEDIS_POOL;

/**
 * @author jzb 2019-06-04
 */
public class LuceneTest {
    private static final Vertx vertx = Vertx.vertx();

    static {
        // sshfs -o allow_other root@10.2.0.215:/data/mes/auto/db /data/mes-3000/auto/db
        System.setProperty("japp.mes.auto.path", "/data/mes-3000/auto");
        INJECTOR = Guice.createInjector(new GuiceModule(vertx), new ReportModule());

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
        JEDIS_POOL = new JedisPool(poolConfig, redisOptions.getHost(), 6379, 100000);
    }

    public static void main(String[] args) {
        final LocalDateTime startLdt = LocalDateTime.of(2019, 8, 22, 7, 22);
        final LocalDateTime endLdt = LocalDateTime.of(2019, 8, 23, 7, 22);
        final StrippingReport report = StrippingReport.create(Workshops.D.getId(), J.date(startLdt).getTime(), J.date(endLdt).getTime());
        System.out.println(report.toJsonNode());
    }
}
