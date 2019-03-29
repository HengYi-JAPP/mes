package com.hengyi.japp.mes.auto.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.application.event.ToDtyEvent;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.domain.data.RoleType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.exception.DoffingTagException;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.flowables.GroupedFlowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import java.security.Principal;
import java.util.*;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class SilkCarRecordServiceImpl implements SilkCarRecordService {
    private final AuthService authService;
    private final DoffingSpecService doffingSpecService;
    private final SilkCarRecordRepository silkCarRecordRepository;
    private final LineRepository lineRepository;
    private final SilkCarRepository silkCarRepository;
    private final GradeRepository gradeRepository;
    private final SilkCarRuntimeRepository silkCarRuntimeRepository;
    private final SilkBarcodeRepository silkBarcodeRepository;
    private final SilkRepository silkRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private SilkCarRecordServiceImpl(AuthService authService, DoffingSpecService doffingSpecService, SilkCarRecordRepository silkCarRecordRepository, LineRepository lineRepository, SilkCarRepository silkCarRepository, GradeRepository gradeRepository, SilkCarRuntimeRepository silkCarRuntimeRepository, SilkBarcodeRepository silkBarcodeRepository, SilkRepository silkRepository, OperatorRepository operatorRepository) {
        this.authService = authService;
        this.doffingSpecService = doffingSpecService;
        this.silkCarRecordRepository = silkCarRecordRepository;
        this.lineRepository = lineRepository;
        this.silkCarRepository = silkCarRepository;
        this.gradeRepository = gradeRepository;
        this.silkCarRuntimeRepository = silkCarRuntimeRepository;
        this.silkBarcodeRepository = silkBarcodeRepository;
        this.silkRepository = silkRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<SilkCarRecord> save(SilkCarRuntime silkCarRuntime) {
        final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
        silkCarRecord.setEndDateTime(new Date());
        silkCarRecord.events(silkCarRuntime.getEventSources());
        return silkCarRecordRepository.save(silkCarRecord);
    }

    @Override
    public Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeInitEvent.ManualDoffingCheckSilksCommand command) {
        final var line$ = lineRepository.find(command.getLine().getId());
        final var silkCar$ = silkCarRepository.findByCode(command.getSilkCar().getCode());
        return line$.flatMap(line -> silkCar$.map(silkCar -> doffingSpecService.checkSilks(DoffingType.MANUAL, line, silkCar)));
    }

    @Override
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.ManualDoffingCommand command) {
        final var event$ = operatorRepository.find(principal).flatMap(operator -> {
            final SilkCarRuntimeInitEvent event = new SilkCarRuntimeInitEvent();
            event.fire(operator);
            return gradeRepository.find(command.getGrade().getId()).flatMap(grade -> {
                event.setGrade(grade);
                return silkCarRepository.findByCode(command.getSilkCar().getCode());
            }).map(silkCar -> {
                event.setSilkCar(silkCar);
                return event;
            });
        });
        final var line$ = lineRepository.find(command.getLine().getId());
        return line$.flatMap(line -> event$.flatMap(event -> {
            final SilkCarRuntimeService silkCarRuntimeService = Jvertx.getProxy(SilkCarRuntimeService.class);
            final var checkSilks = command.getCheckSilks();
            final var silkCar = event.getSilkCar();
            return doffingSpecService.generateSilkRuntimes(DoffingType.MANUAL, line, silkCar, checkSilks).toList().flatMap(silkRuntimes -> {
                event.setSilkRuntimes(silkRuntimes);
                return silkCarRuntimeService.doffing(event, DoffingType.MANUAL);
            });
        }));
    }

    @Override
    public Completable handle(Principal principal, ToDtyEvent.Command command) {
        final var event$ = operatorRepository.find(principal).map(operator -> {
            final ToDtyEvent event = new ToDtyEvent();
            event.fire(operator);
            event.setCommand(MAPPER.convertValue(command, JsonNode.class));
            return event;
        });
        final SilkCarRuntimeService silkCarRuntimeService = Jvertx.getProxy(SilkCarRuntimeService.class);
        return silkCarRuntimeService.find(command.getSilkCarRecord()).flatMapCompletable(silkCarRuntime -> {
            final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
            final Batch batch = silkCarRecord.getBatch();
            final Product product = batch.getProduct();
            if (!"POY".equals(product.getName())) {
                throw new RuntimeException("POY可以推加弹！");
            }
            return event$.flatMapCompletable(event -> silkCarRuntimeRepository.addEventSource(silkCarRecord, event));
        });
    }

    @Override
    public Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeInitEvent.AutoDoffingOverWriteCheckSilksCommand command) {
        final var result$ = silkCarRuntimeRepository.findByCode(command.getSilkCar().getCode()).flattenAsFlowable(silkCarRuntime -> {
            final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
            if (DoffingType.AUTO != silkCarRecord.getDoffingType()) {
                throw new RuntimeException("非自动落筒，无法换人工标签");
            }
            final boolean present = J.emptyIfNull(silkCarRuntime.getEventSources())
                    .parallelStream()
                    .filter(it -> !it.isDeleted())
                    .findFirst()
                    .isPresent();
            if (present) {
                throw new RuntimeException("已有其他人对丝车进行操作，无法换人工标签");
            }
            return J.emptyIfNull(silkCarRuntime.getSilkRuntimes());
        }).groupBy(it -> {
            final Silk silk = it.getSilk();
            final String silkCode = silk.getCode();
            return SilkBarcodeService.silkCodeToSilkBarCode(silkCode);
        }).flatMapSingle(GroupedFlowable::toList).map(silkRuntimes -> {
            final List<SilkRuntime> list = Lists.newArrayList(silkRuntimes);
            Collections.shuffle(list);
            final SilkRuntime silkRuntime = list.get(0);
            final CheckSilkDTO checkSilk = new CheckSilkDTO();
            checkSilk.setSideType(silkRuntime.getSideType());
            checkSilk.setRow(silkRuntime.getRow());
            checkSilk.setCol(silkRuntime.getCol());
            return checkSilk;
        }).toList();
        return authService.checkRole(principal, RoleType.DOFFING).andThen(result$);
    }

    @Override
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.AutoDoffingOverWriteCommand command) {
        final var result$ = silkCarRuntimeRepository.findByCode(command.getSilkCar().getCode()).flatMapSingle(silkCarRuntime -> {
            final Collection<SilkRuntime> silkRuntimes = silkCarRuntime.getSilkRuntimes();
            final Completable replace$ = Flowable.fromIterable(command.getCheckSilks()).flatMapCompletable(checkSilk -> {
                final SilkRuntime silkRuntime = silkRuntimes.parallelStream()
                        .filter(it -> Objects.equals(it.getSideType(), checkSilk.getSideType()) &&
                                Objects.equals(it.getRow(), checkSilk.getRow()) &&
                                Objects.equals(it.getCol(), checkSilk.getCol())
                        ).findFirst().get();
                final String code = SilkBarcodeService.silkCodeToSilkBarCode(checkSilk.getCode());
                return silkBarcodeRepository.findByCode(code).flatMapCompletable(silkBarcode ->
                        tryReplace(silkRuntimes, checkSilk, silkBarcode, silkRuntime)
                );
            });
            return replace$.andThen(Single.fromCallable(() -> silkCarRuntime));
        }).flatMap(silkCarRuntime -> {
            final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
            silkCarRecord.setDoffingType(DoffingType.MANUAL);
            final var saveSilkCarRecord$ = silkCarRecordRepository.save(silkCarRecord).ignoreElement();
            final var saveSilks$ = Flowable.fromIterable(silkCarRuntime.getSilkRuntimes()).flatMapSingle(silkRuntime -> {
                final Silk silk = silkRuntime.getSilk();
                return silkRepository.save(silk);
            }).ignoreElements();
            return Completable.mergeArray(saveSilkCarRecord$, saveSilks$).toSingleDefault(silkCarRuntime);
        });
        return authService.checkRole(principal, RoleType.DOFFING).andThen(result$);
    }

    private Completable tryReplace(Collection<SilkRuntime> silkRuntimes, CheckSilkDTO checkSilk, SilkBarcode checkSilkBarcode, SilkRuntime silkRuntime) {
        return Completable.fromAction(() -> {
            @NotBlank final String checkSilk_code = checkSilk.getCode();
            final String checkSilk_SilkBarcode_code = checkSilkBarcode.getCode();
            final String checkSilk_spindle = StringUtils.replace(checkSilk_code, checkSilk_SilkBarcode_code, "");
            final Silk silk = silkRuntime.getSilk();
            final String silk_code = silk.getCode();
            final String silk_SilkBarcode_code = SilkBarcodeService.silkCodeToSilkBarCode(silk_code);
            final String silk_spindle = StringUtils.replace(silk_code, silk_SilkBarcode_code, "");
            if (!Objects.equals(checkSilkBarcode.getLineMachine(), silk.getLineMachine())) {
                throw new RuntimeException("code[" + checkSilk_code + "]，机台贴错！");
            }
            if (!Objects.equals(checkSilk_spindle, silk_spindle)) {
                throw new DoffingTagException();
            }
            silkRuntimes.parallelStream().filter(it -> {
                final Silk itSilk = it.getSilk();
                return StringUtils.startsWith(itSilk.getCode(), silk_SilkBarcode_code);
            }).forEach(it -> {
                final Silk itSilk = it.getSilk();
                final String code = itSilk.getCode();
                final String newCode = StringUtils.replace(code, silk_SilkBarcode_code, checkSilk_SilkBarcode_code);
                itSilk.setDoffingNum(checkSilkBarcode.getDoffingNum());
                itSilk.setCode(newCode);
            });
        });
    }
}
