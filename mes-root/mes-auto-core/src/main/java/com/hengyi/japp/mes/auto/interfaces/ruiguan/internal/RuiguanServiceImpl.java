package com.hengyi.japp.mes.auto.interfaces.ruiguan.internal;

import com.github.ixtf.japp.core.J;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.PrintCommand;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.domain.data.MesAutoPrinter;
import com.hengyi.japp.mes.auto.interfaces.ruiguan.RuiguanService;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.reactivex.redis.RedisClient;
import org.apache.commons.lang3.RandomUtils;

import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.domain.SilkBarcode.INIT_LD;
import static com.hengyi.japp.mes.auto.domain.SilkBarcode.RADIX;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-03-11
 */
@Singleton
public class RuiguanServiceImpl implements RuiguanService {
    private final RedisClient redisClient;

    @Inject
    private RuiguanServiceImpl(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public Completable handle(Principal principal, SilkCarRuntimeInitEvent.RuiguanAutoDoffingCommand command) {
        final String id = command.getId();
        final var silkCarInfo = command.getSilkCarInfo();
        final MesAutoPrinter printer = new MesAutoPrinter();
        printer.setId("test");
        printer.setName("C5");
        return Single.just(String.join("-", "SilkBarcodePrinter", printer.getId(), printer.getName()))
                .flatMap(channel -> {
                    final var silks = command.getSilkInfos().stream().map(silkInfo -> {
                        final var item = new PrintCommand.Item();
                        item.setBatchNo(silkCarInfo.getBatchNo());
                        item.setBatchSpec(silkCarInfo.getBatchSpec());
                        item.setLineName(silkInfo.getLine());
                        item.setLineMachineItem(silkInfo.getLineMachine());
                        item.setSpindle(silkInfo.getSpindle());

                        item.setCodeDate(command.getCreateDateTime());
                        final LocalDate codeLd = J.localDate(item.getCodeDate());
                        final long between = ChronoUnit.DAYS.between(INIT_LD, codeLd);
                        String s = Long.toString(between, RADIX);
                        final String dateCode = Strings.padStart(s, 4, '0');
                        s = Long.toString(RandomUtils.nextLong(), RADIX);
                        final String codeDoffingNumCode = Strings.padStart(s, 5, '0');
                        item.setCode(dateCode + codeDoffingNumCode);
                        return item;
                    }).collect(toList());
                    final String message = MAPPER.writeValueAsString(silks);
//                    System.out.println(message);
//                    return Single.just(message);
                    return redisClient.rxPublish(channel, message);
                })
                .ignoreElement()
                .doOnComplete(() -> {
                    final StringBuilder sb = new StringBuilder("SilkCarRecord[").append(id).append("]:").append("打印成功");
                    LOG.info(sb.toString());
                }).doOnError(ex -> {
                    final StringBuilder sb = new StringBuilder("SilkCarRecord[").append(id).append("]:").append("打印失败");
                    LOG.info(sb.toString());
                });
    }

}
