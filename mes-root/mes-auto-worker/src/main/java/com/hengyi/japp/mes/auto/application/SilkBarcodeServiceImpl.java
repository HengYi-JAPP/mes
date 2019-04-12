package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.PrintCommand;
import com.hengyi.japp.mes.auto.application.command.SilkBarcodeGenerateCommand;
import com.hengyi.japp.mes.auto.application.query.SilkBarcodeQuery;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.MesAutoPrinter;
import com.hengyi.japp.mes.auto.repository.BatchRepository;
import com.hengyi.japp.mes.auto.repository.LineMachineRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vertx.reactivex.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;

import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class SilkBarcodeServiceImpl implements SilkBarcodeService {
    private final RedisClient redisClient;
    private final SilkBarcodeRepository silkBarcodeRepository;
    private final LineMachineRepository lineMachineRepository;
    private final BatchRepository batchRepository;
    private final OperatorRepository operatorRepository;
    private final ExecutorService es = Executors.newSingleThreadExecutor();

    @Inject
    private SilkBarcodeServiceImpl(RedisClient redisClient, SilkBarcodeRepository silkBarcodeRepository, LineMachineRepository lineMachineRepository, BatchRepository batchRepository, OperatorRepository operatorRepository) {
        this.redisClient = redisClient;
        this.silkBarcodeRepository = silkBarcodeRepository;
        this.lineMachineRepository = lineMachineRepository;
        this.batchRepository = batchRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<SilkBarcode> findBySilkCode(String code) {
        final String s = SilkBarcodeService.silkCodeToSilkBarCode(code);
        return silkBarcodeRepository.findByCode(s);
    }

    @Override
    public Single<SilkBarcode> generate(Principal principal, SilkBarcodeGenerateCommand command) {
        final Single<Operator> operator$ = operatorRepository.find(principal);
        final Single<LineMachine> lineMachine$ = lineMachineRepository.find(command.getLineMachine().getId());
        return operator$.flatMap(operator -> lineMachine$.map(lineMachine -> {
            final LineMachineProductPlan productPlan = lineMachine.getProductPlan();
            final Batch batch = productPlan.getBatch();
            final LocalDate codeLd = J.localDate(command.getCodeDate());
            final String doffingNum = command.getDoffingNum();
            return generate(operator, codeLd, lineMachine, batch, doffingNum);
        }));
    }

    synchronized private SilkBarcode generate(Operator operator, LocalDate codeLd, LineMachine lineMachine, Batch batch, String doffingNum) {
        final SilkBarcodeQuery silkBarcodeQuery = SilkBarcodeQuery.builder()
                .startLd(codeLd)
                .endLd(codeLd)
                .lineMachineId(lineMachine.getId())
                .doffingNum(doffingNum)
                .batchId(batch.getId())
                .build();
        final Collection<SilkBarcode> silkBarcodes = Single.just(silkBarcodeQuery)
                .subscribeOn(Schedulers.from(es))
                .flatMap(silkBarcodeRepository::query)
                .map(SilkBarcodeQuery.Result::getSilkBarcodes)
                .blockingGet();
        if (J.nonEmpty(silkBarcodes)) {
            return IterableUtils.get(silkBarcodes, 0);
        }
        return silkBarcodeRepository.create().flatMap(silkBarcode -> {
            silkBarcode.setCodeDate(J.date(codeLd));
            silkBarcode.setDoffingNum(doffingNum);
            silkBarcode.setBatch(batch);
            silkBarcode.setLineMachine(lineMachine);
            silkBarcode.log(operator);
            return nextCodeDoffingNum(silkBarcode.getCodeDate()).flatMap(codeDoffingNum -> {
                silkBarcode.setCodeDoffingNum(codeDoffingNum);
                return silkBarcodeRepository.save(silkBarcode);
            });
        }).blockingGet();
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
    public Flowable<SilkBarcode> generate(Principal principal, SilkBarcodeGenerateCommand.Batch commands) {
        final Single<Operator> operator$ = operatorRepository.find(principal);
        return operator$.flatMapPublisher(operator -> Flowable.fromIterable(commands.getCommands()).flatMapSingle(command -> {
            final LocalDate codeLd = J.localDate(command.getCodeDate());
            final String doffingNum = command.getDoffingNum();
            return lineMachineRepository.find(command.getLineMachine().getId()).map(lineMachine -> {
                final LineMachineProductPlan productPlan = lineMachine.getProductPlan();
                final Batch batch = productPlan.getBatch();
                return generate(operator, codeLd, lineMachine, batch, doffingNum);
            });
        }));
    }

    @Override
    public Flowable<SilkBarcode> generate(Principal principal, SilkBarcodeGenerateCommand.BatchAndBatch commands) {
        final Single<Operator> operator$ = operatorRepository.find(principal);
        final Single<Batch> batch$ = batchRepository.find(commands.getBatch().getId());
        return operator$.flatMapPublisher(operator -> batch$.flatMapPublisher(batch -> Flowable.fromIterable(commands.getCommands()).flatMapSingle(command -> {
            final LocalDate codeLd = J.localDate(command.getCodeDate());
            final String doffingNum = command.getDoffingNum();
            return lineMachineRepository.find(command.getLineMachine().getId()).map(lineMachine ->
                    generate(operator, codeLd, lineMachine, batch, doffingNum)
            );
        })));
    }

    @Override
    public Completable print(Principal principal, MesAutoPrinter printer, List<SilkBarcode> silkBarcodes) {
        return Flowable.fromIterable(silkBarcodes).map(silkBarcode -> {
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
        }).flatMap(Flowable::fromIterable).toList().flatMapCompletable(it -> print(printer, it));
    }

    @Override
    public Completable print(MesAutoPrinter printer, Collection<PrintCommand.Item> silks) {
        return Single.just(String.join("-", "SilkBarcodePrinter", printer.getId(), printer.getName()))
                .flatMap(channel -> {
                    final List<PrintCommand.Item> list = Lists.newArrayList(silks);
                    Collections.sort(list);
                    final String message = MAPPER.writeValueAsString(list);
                    return redisClient.rxPublish(channel, message);
                })
                .ignoreElement();
    }

}
