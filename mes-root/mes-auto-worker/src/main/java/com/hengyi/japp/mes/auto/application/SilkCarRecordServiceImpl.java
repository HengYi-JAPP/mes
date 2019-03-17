package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.Date;
import java.util.List;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class SilkCarRecordServiceImpl implements SilkCarRecordService {
    private final DoffingSpecService doffingSpecService;
    private final SilkCarRecordRepository silkCarRecordRepository;
    private final LineRepository lineRepository;
    private final SilkCarRepository silkCarRepository;
    private final GradeRepository gradeRepository;
    private final SilkRepository silkRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private SilkCarRecordServiceImpl(DoffingSpecService doffingSpecService, SilkCarRecordRepository silkCarRecordRepository, LineRepository lineRepository, SilkCarRepository silkCarRepository, GradeRepository gradeRepository, SilkRepository silkRepository, OperatorRepository operatorRepository) {
        this.doffingSpecService = doffingSpecService;
        this.silkCarRecordRepository = silkCarRecordRepository;
        this.lineRepository = lineRepository;
        this.silkCarRepository = silkCarRepository;
        this.gradeRepository = gradeRepository;
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

}
