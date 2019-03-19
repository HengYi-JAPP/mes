package doffing;

import com.google.inject.Guice;
import com.hengyi.japp.mes.auto.doffing.DoffingModule;
import com.hengyi.japp.mes.auto.doffing.application.DoffingService;
import com.hengyi.japp.mes.auto.doffing.domain.AutoDoffingSilkCarRecordAdapt;
import com.hengyi.japp.mes.auto.doffing.domain.AutoDoffingSilkCarRecordAdaptHistory;
import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import lombok.Data;
import lombok.SneakyThrows;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.persistence.EntityManager;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.doffing.Doffing.INJECTOR;

/**
 * @author jzb 2019-03-08
 */
public class DoffingTest {
    public static final LocalDate INIT_LD = LocalDate.of(2019, Month.MARCH, 1);
    public static final int RADIX = Character.MAX_RADIX;
    private static final JedisPool JEDIS_POOL = new JedisPool(new JedisPoolConfig(), "192.168.0.38");

    @SneakyThrows
    public static void main(String[] args) {
        System.out.println(Long.parseLong("zzzzzz", Character.MAX_RADIX));
        System.out.println(new Date().getTime() / 1000);

        final Vertx vertx = Vertx.vertx();
        INJECTOR = Guice.createInjector(new DoffingModule(vertx));

        testPrint("C420190319095453YJ036P0866");
//        doffingService.fetch().flatMapSingle(doffingService::toMessageBody)
//                .subscribe(System.out::println);
//        restore();

        TimeUnit.DAYS.sleep(1);
    }

    private static void testPrint(String id) {
        final RabbitMQClient rabbitMQClient = INJECTOR.getInstance(RabbitMQClient.class);
        final DoffingService doffingService = INJECTOR.getInstance(DoffingService.class);
        final EntityManager em = INJECTOR.getInstance(EntityManager.class);
        final var silkCarRecord = em.find(AutoDoffingSilkCarRecordAdaptHistory.class, id);
        final AutoDoffingSilkCarRecordAdapt data = MAPPER.convertValue(silkCarRecord, AutoDoffingSilkCarRecordAdapt.class);
        rabbitMQClient.rxStart().andThen(doffingService.toMessageBody(data))
                .flatMapCompletable(body -> {
                    System.out.println(body);

                    final JsonObject message = new JsonObject().put("body", body);
                    rabbitMQClient.rxBasicPublish("mes.auto.doffing.9200", "", message);
                    final String channel = String.join("-", "SilkBarcodePrinter", "test", "C6");
//                    final MessageBoy messageBoy = MAPPER.readValue(body, MessageBoy.class);
//                    final MessageBoy.SilkCarInfo silkCarInfo = messageBoy.getSilkCarInfo();
//
//                    final var silks = messageBoy.getSilkInfos().stream().map(silkInfo -> {
//                        final var item = new PrintItem();
//                        item.setBatchNo(silkCarInfo.getBatchNo());
//                        item.setBatchSpec(silkCarInfo.getBatchSpec());
//                        item.setLineName(silkInfo.getLine());
//                        item.setLineMachineItem(silkInfo.getLineMachine());
//                        item.setSpindle(silkInfo.getSpindle());
//
//                        item.setCodeDate(messageBoy.getCreateDateTime());
//                        final LocalDate codeLd = J.localDate(item.getCodeDate());
//                        final long between = ChronoUnit.DAYS.between(INIT_LD, codeLd);
//                        String s = Long.toString(between, RADIX);
//                        final String dateCode = Strings.padStart(s, 4, '0');
//                        s = Long.toString(silkInfo.getTimestamp(), RADIX);
//                        if (s.length() > 5) {
//                            s = s.substring(0, 5);
//                        }
//                        final String codeDoffingNumCode = Strings.padStart(s, 5, '0').substring(0, 5);
//                        final String spindleCode = Strings.padStart("" + silkInfo.getSpindle(), 2, '0');
//                        item.setCode((dateCode + codeDoffingNumCode + spindleCode + "C").toUpperCase());
//                        return item;
//                    }).collect(toList());
//                    final String message = MAPPER.writeValueAsString(silks);
//                    try (final Jedis jedis = JEDIS_POOL.getResource()) {
//                        jedis.publish(channel, message);
//                    }
                    return Completable.complete();
                })
                .subscribe();
    }

    private static void restore() {
        final EntityManager em = INJECTOR.getInstance(EntityManager.class);
        em.getTransaction().begin();
        em.createQuery("select o from AutoDoffingSilkCarRecordAdaptHistory o").getResultList().forEach(it -> {
            final var data = MAPPER.convertValue(it, AutoDoffingSilkCarRecordAdapt.class);
            data.setModifyDateTime(null);
            data.setState(0);
            em.merge(data);
            em.remove(it);
        });
        em.getTransaction().commit();
    }

    @Data
    public static class PrintItem implements Serializable {
        @NotBlank
        private String code;
        @NotNull
        private Date codeDate;
        @NotBlank
        private String lineName;
        @NotBlank
        private int lineMachineItem;
        @Min(1)
        private int spindle;
        private String doffingNum;
        @NotBlank
        private String batchNo;
        @NotBlank
        private String batchSpec;
    }

}
