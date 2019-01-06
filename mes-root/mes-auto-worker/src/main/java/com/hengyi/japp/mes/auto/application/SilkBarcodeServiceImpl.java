package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.PrintCommand;
import com.hengyi.japp.mes.auto.application.command.SilkBarcodeGenerateCommand;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.SilkBarcode;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.LineMachineRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import io.vertx.reactivex.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.Constant.AMQP.MES_AUTO_PRINT_EXCHANGE;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class SilkBarcodeServiceImpl implements SilkBarcodeService {
    private final RabbitMQClient rabbitClient;
    private final RedisClient redisClient;
    private final SilkBarcodeRepository silkBarcodeRepository;
    private final LineMachineRepository lineMachineRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private SilkBarcodeServiceImpl(RabbitMQClient rabbitClient, RedisClient redisClient, SilkBarcodeRepository silkBarcodeRepository, LineMachineRepository lineMachineRepository, OperatorRepository operatorRepository) {
        this.rabbitClient = rabbitClient;
        this.redisClient = redisClient;
        this.silkBarcodeRepository = silkBarcodeRepository;
        this.lineMachineRepository = lineMachineRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<SilkBarcode> findBySilkCode(String code) {
        final String s = SilkBarcodeService.silkCodeToSilkBarCode(code);
        return silkBarcodeRepository.findByCode(s);
    }

    @Override
    public Single<SilkBarcode> generate(Principal principal, SilkBarcodeGenerateCommand command) {
        return lineMachineRepository.find(command.getLineMachine().getId()).flatMap(lineMachine -> {
            final LocalDate codeDate = J.localDate(command.getCodeDate());
            final String doffingNum = command.getDoffingNum();
            return silkBarcodeRepository.find(codeDate, lineMachine, doffingNum, lineMachine.getProductPlan().getBatch())
                    .switchIfEmpty(create(principal, codeDate, lineMachine, doffingNum));
        });
    }

    private synchronized Single<SilkBarcode> create(Principal principal, LocalDate codeLd, LineMachine lineMachine, String doffingNum) {
        return silkBarcodeRepository.find(codeLd, lineMachine, doffingNum, lineMachine.getProductPlan().getBatch())
                .switchIfEmpty(silkBarcodeRepository.create())
                .flatMap(silkBarcode -> {
                    silkBarcode.setCodeDate(J.date(codeLd));
                    silkBarcode.setDoffingNum(doffingNum);
                    silkBarcode.setLineMachine(lineMachine);
                    silkBarcode.setBatch(lineMachine.getProductPlan().getBatch());

                    return nextCodeDoffingNum(silkBarcode.getCodeDate()).flatMap(codeDoffingNum -> {
                        silkBarcode.setCodeDoffingNum(codeDoffingNum);
                        return operatorRepository.find(principal);
                    }).flatMap(operator -> {
                        silkBarcode.log(operator);
                        return silkBarcodeRepository.save(silkBarcode);
                    });
                });
    }

    private Single<Long> nextCodeDoffingNum(Date date) {
        final String incrKey = SilkBarcodeService.key(date);
        return redisClient.rxIncr(incrKey).doAfterTerminate(() -> redisClient.rxTtl(incrKey)
                // key 存在，但没有设置剩余生存时间
                .filter(it -> it == -1)
                .flatMapSingleElement(it -> {
                    // 一年后过期
                    final long seconds = ChronoUnit.YEARS.getDuration().getSeconds();
                    return redisClient.rxExpire(incrKey, seconds);
                })
                .subscribe());
    }

    @Override
    public Completable print(Principal principal, PrintCommand.SilkBarcodePrintCommand command) {
        return Flowable.fromIterable(command.getSilkBarcodes())
                .map(EntityDTO::getId)
                .flatMapSingle(silkBarcodeRepository::find)
                .map(silkBarcode -> {
                    final Collection<SilkBarcode.SilkInfo> silkInfos = silkBarcode.listSilkInfo();
                    final Map<Integer, SilkBarcode.SilkInfo> map = silkInfos.stream()
                            .collect(Collectors.toMap(SilkBarcode.SilkInfo::getSpindle, Function.identity()));
                    final LineMachine lineMachine = silkBarcode.getLineMachine();
                    final List<PrintCommand.Item> result = Lists.newArrayList();
                    for (Integer spindle : lineMachine.getSpindleSeq()) {
                        final SilkBarcode.SilkInfo silkInfo = map.get(spindle);
                        final PrintCommand.Item item = new PrintCommand.Item();
                        final Date codeDate = silkInfo.getCodeDate();
                        final Line line = lineMachine.getLine();
                        final String doffingNum = silkInfo.getDoffingNum();
                        final Batch batch = silkInfo.getBatch();
                        final String batchNo = batch.getBatchNo();
                        final String spec = batch.getSpec();
                        final String code = silkInfo.getCode();
                        item.setCode(code);
                        item.setCodeDate(codeDate);
                        item.setLineName(line.getName());
                        item.setLineMachineItem(lineMachine.getItem());
                        item.setSpindle(spindle);
                        item.setDoffingNum(doffingNum);
                        item.setBatchNo(batchNo);
                        item.setBatchSpec(spec);
                        result.add(item);
                    }
                    return result;
                }).flatMap(Flowable::fromIterable).toList()
                .flatMapCompletable(it -> print(command.getMesAutoPrinter().getId(), it));
    }

    @Override
    public Completable print(Principal principal, PrintCommand.SilkPrintCommand command) {
        return print(command.getMesAutoPrinter().getId(), command.getSilks());
    }

    @Override
    public Single<SilkBarcode> changeBatch(Principal principal, String id) {
        return silkBarcodeRepository.find(id).flatMap(oldSilkBarcode -> {
            final Batch oldBatch = oldSilkBarcode.getBatch();
            final LineMachine lineMachine = oldSilkBarcode.getLineMachine();
            final Batch newBatch = lineMachine.getProductPlan().getBatch();
            if (Objects.equals(oldBatch, newBatch)) {
                return Single.just(oldSilkBarcode);
            }

            return silkBarcodeRepository.create().flatMap(silkBarcode -> {
                silkBarcode.setBatch(newBatch);
                silkBarcode.setCodeDate(oldSilkBarcode.getCodeDate());
                silkBarcode.setLineMachine(oldSilkBarcode.getLineMachine());
                silkBarcode.setDoffingNum(oldSilkBarcode.getDoffingNum());
                return operatorRepository.find(principal).flatMap(operator -> {
                    silkBarcode.log(operator);
                    return silkBarcodeRepository.save(silkBarcode);
                });
            });
        });
    }

    private Completable print(String rk, Collection<PrintCommand.Item> silks) {
        return Single.fromCallable(() -> {
            final ArrayList<PrintCommand.Item> list = Lists.newArrayList(silks);
            Collections.sort(list);
            final String body = MAPPER.writeValueAsString(list);
            return new JsonObject().put("body", body);
        }).flatMapCompletable(it -> {
            final Completable completable = rabbitClient.rxBasicPublish(MES_AUTO_PRINT_EXCHANGE, rk, it);
            return rabbitClient.rxStart().andThen(completable);
        });
    }
}
