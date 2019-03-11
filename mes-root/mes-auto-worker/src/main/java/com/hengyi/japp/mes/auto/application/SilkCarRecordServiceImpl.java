package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkCarRecordRepository;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class SilkCarRecordServiceImpl implements SilkCarRecordService {
    private final SilkCarRecordRepository silkCarRecordRepository;
    private final SilkRepository silkRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private SilkCarRecordServiceImpl(SilkCarRecordRepository silkCarRecordRepository, SilkRepository silkRepository, OperatorRepository operatorRepository) {
        this.silkCarRecordRepository = silkCarRecordRepository;
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
    public void handle(SilkCarRuntimeInitEvent.AutoDoffingSilkCarRuntimeCreateCommand command) {

    }

}
