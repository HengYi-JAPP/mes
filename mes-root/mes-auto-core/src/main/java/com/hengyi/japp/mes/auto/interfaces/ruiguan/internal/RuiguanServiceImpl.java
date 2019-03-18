package com.hengyi.japp.mes.auto.interfaces.ruiguan.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.PrintCommand;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.interfaces.ruiguan.RuiguanService;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.redis.RedisClient;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.validation.constraints.NotBlank;
import java.security.Principal;
import java.util.*;
import java.util.function.BiFunction;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-03-11
 */
@Singleton
public class RuiguanServiceImpl implements RuiguanService {
    private final RedisClient redisClient;
    private final SilkCarRecordRepository silkCarRecordRepository;
    private final BatchRepository batchRepository;
    private final LineRepository lineRepository;
    private final LineMachineRepository lineMachineRepository;
    private final SilkBarcodeRepository silkBarcodeRepository;
    private final SilkRepository silkRepository;
    private final GradeRepository gradeRepository;
    private final SilkCarRepository silkCarRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private RuiguanServiceImpl(RedisClient redisClient, SilkCarRecordRepository silkCarRecordRepository, BatchRepository batchRepository, LineRepository lineRepository, LineMachineRepository lineMachineRepository, SilkRepository silkRepository, SilkBarcodeRepository silkBarcodeRepository, GradeRepository gradeRepository, SilkCarRepository silkCarRepository, OperatorRepository operatorRepository) {
        this.redisClient = redisClient;
        this.silkCarRecordRepository = silkCarRecordRepository;
        this.batchRepository = batchRepository;
        this.lineRepository = lineRepository;
        this.lineMachineRepository = lineMachineRepository;
        this.silkRepository = silkRepository;
        this.silkBarcodeRepository = silkBarcodeRepository;
        this.gradeRepository = gradeRepository;
        this.silkCarRepository = silkCarRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Completable handle(Principal principal, SilkCarRuntimeInitEvent.RuiguanAutoDoffingCommand command) {
        final String id = command.getId();
        return silkCarRecordRepository.findByAutoId(id).switchIfEmpty(create(principal, command)).ignoreElement();
    }

    private Single<SilkCarRecord> create(Principal principal, SilkCarRuntimeInitEvent.RuiguanAutoDoffingCommand command) {
        final var silkCarInfo = command.getSilkCarInfo();
        final SilkCarRuntimeInitEvent event = new SilkCarRuntimeInitEvent();
        return operatorRepository.find(principal).flatMap(operator -> {
            event.fire(operator);
            event.setCommand(MAPPER.convertValue(command, JsonNode.class));
            return silkCarRepository.findByCode(silkCarInfo.getCode());
        }).flatMap(silkCar -> {
            event.setSilkCar(silkCar);
            return gradeRepository.findByName(silkCarInfo.getGrade()).toSingle();
        }).flatMap(grade -> {
            event.setGrade(grade);
            return batchRepository.findByBatchNo(silkCarInfo.getBatchNo()).toSingle();
        }).flatMap(batch -> Flowable.fromIterable(command.getSilkInfos()).groupBy(it -> {
            final String lineName = it.getLine();
            final int lineMachineItem = it.getLineMachine();
            final long timestamp = it.getTimestamp();
            return Triple.of(lineName, lineMachineItem, timestamp);
        }).flatMap(groupedFlowable -> {
            final var triple = groupedFlowable.getKey();
            final String lineName = triple.getLeft();
            return lineRepository.findByName(lineName).flatMap(line -> {
                final int lineMachineItem = triple.getMiddle();
                return lineMachineRepository.findBy(line, lineMachineItem);
            }).flatMap(lineMachine -> {
                final long timestamp = triple.getRight();
                return silkBarcodeRepository.findByAuto(principal, lineMachine, batch, timestamp);
            }).flatMapPublisher(silkBarcode -> groupedFlowable.flatMapSingle(silkInfo -> {
                final SilkRuntime silkRuntime = new SilkRuntime();
                silkRuntime.setSideType(silkInfo.getSideType());
                silkRuntime.setRow(silkInfo.getRow());
                silkRuntime.setCol(silkInfo.getCol());
                final int spindle = silkInfo.getSpindle();
                final String silkCode = silkBarcode.generateSilkCode(spindle);
                return silkRepository.findByCodeOrCreate(silkCode).map(silk -> {
                    silkRuntime.setSilk(silk);
                    silk.setCode(silkCode);
                    silk.setDoffingNum("");
                    silk.setLineMachine(silkBarcode.getLineMachine());
                    silk.setSpindle(spindle);
                    silk.setBatch(batch);
                    silk.setGrade(event.getGrade());
                    silk.setDoffingOperator(event.getOperator());
                    silk.setDoffingType(DoffingType.AUTO);
                    silk.setDoffingDateTime(silkInfo.getDoffingDateTime());
                    silk.setDoffingOperator(event.getOperator());
                    return silkRuntime;
                });
            }));
        }).toList().flatMap(silkRuntimes -> {
            event.setSilkRuntimes(silkRuntimes);
            return silkCarRecordRepository.create().flatMap(silkCarRecord -> {
                silkCarRecord.setId(command.getId());
                silkCarRecord.setSilkCar(event.getSilkCar());
                silkCarRecord.setBatch(batch);
                silkCarRecord.setGrade(event.getGrade());
                silkCarRecord.setDoffingOperator(event.getOperator());
                silkCarRecord.setDoffingType(DoffingType.AUTO);
                silkCarRecord.setDoffingDateTime(event.getFireDateTime());
                silkCarRecord.initEvent(event);
                return silkCarRecordRepository.save(silkCarRecord);
            }).flatMap(silkCarRecord -> Flowable.fromIterable(silkRuntimes)
                    .flatMapSingle(silkRuntime -> {
                        final Silk silk = silkRuntime.getSilk();
                        silk.setSilkCarRecords(Lists.newArrayList(silkCarRecord));
                        return silkRepository.save(silk);
                    })
                    .toList().map(it -> silkCarRecord));
        })).doOnSuccess(it -> printSilk(it).subscribe());
    }

    @Override
    public Completable printSilk(SilkCarRecord silkCarRecord) {
        final List<PrintCommand.Item> items = Lists.newArrayList();
        final SilkCar silkCar = silkCarRecord.getSilkCar();
        return Single.fromCallable(() -> {
            final int row = silkCar.getRow();
            final int col = silkCar.getCol();
            final Collection<SilkRuntime> silkRuntimes = silkCarRecord.initSilks();
            add(items, silkRuntimes, SilkCarSideType.A, row, col);
            add(items, silkRuntimes, SilkCarSideType.B, row, col);
            final String message = MAPPER.writeValueAsString(items);
            @NotBlank final String lineName = items.stream().findAny().get().getLineName();
            @NotBlank final String channel = "SilkBarcodePrinter-ruiguan-" + lineName;
            return Pair.of(channel, message);
        }).flatMap(pair -> redisClient.rxPublish(pair.getKey(), pair.getValue()))
                .ignoreElement()
                .doOnComplete(() -> {
                    final StringBuilder sb = new StringBuilder("SilkCarRecord[")
                            .append(silkCarRecord.getId()).append("]:").append("打印成功");
                    LOG.info(sb.toString());
                })
                .doOnError(ex -> {
                    final StringBuilder sb = new StringBuilder("SilkCarRecord[")
                            .append(silkCarRecord.getId()).append("]:").append("打印失败");
                    LOG.info(sb.toString());
                });
    }

    private void add(List<PrintCommand.Item> items, Collection<SilkRuntime> silkRuntimes, SilkCarSideType sideType, int row, int col) {
        final BiFunction<Integer, Integer, Optional<PrintCommand.Item>> findFun = (i, j) -> silkRuntimes.stream().filter(it ->
                Objects.equals(sideType, it.getSideType()) &&
                        Objects.equals(i, it.getRow()) &&
                        Objects.equals(j, it.getCol())
        ).findFirst().map(silkRuntime -> {
            final Silk silk = silkRuntime.getSilk();
            final Batch batch = silk.getBatch();
            final LineMachine lineMachine = silk.getLineMachine();
            final Line line = lineMachine.getLine();
            final var item = new PrintCommand.Item();
            item.setBatchNo(batch.getBatchNo());
            item.setBatchSpec(batch.getSpec());
            item.setSpindle(silk.getSpindle());
            item.setLineMachineItem(lineMachine.getItem());
            item.setLineName(line.getName());
            item.setCode(silk.getCode());
            item.setDoffingNum("");
            item.setCodeDate(silk.getDoffingDateTime());
            return item;
        });
        boolean reverse = true;
        for (int i = row; i > 0; i--) {
            final List<PrintCommand.Item> rowItems = Lists.newArrayList();
            for (int j = 0; j < col; j++) {
                findFun.apply(i, j).ifPresent(rowItems::add);
            }
            if (reverse) {
                Collections.reverse(rowItems);
            }
            items.addAll(rowItems);
            reverse = false;
        }
    }

}
