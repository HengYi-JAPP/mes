package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.ExceptionRecordUpdateCommand;
import com.hengyi.japp.mes.auto.domain.ExceptionRecord;
import com.hengyi.japp.mes.auto.domain.SilkException;
import com.hengyi.japp.mes.auto.repository.ExceptionRecordRepository;
import com.hengyi.japp.mes.auto.repository.LineMachineRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkExceptionRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.Date;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class ExceptionRecordServiceImpl implements ExceptionRecordService {
    private final ExceptionRecordRepository exceptionRecordRepository;
    private final LineMachineRepository lineMachineRepository;
    private final SilkExceptionRepository silkExceptionRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private ExceptionRecordServiceImpl(ExceptionRecordRepository exceptionRecordRepository, LineMachineRepository lineMachineRepository, SilkExceptionRepository silkExceptionRepository, OperatorRepository operatorRepository) {
        this.exceptionRecordRepository = exceptionRecordRepository;
        this.lineMachineRepository = lineMachineRepository;
        this.silkExceptionRepository = silkExceptionRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<ExceptionRecord> create(Principal principal, ExceptionRecordUpdateCommand command) {
        return exceptionRecordRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<ExceptionRecord> save(Principal principal, ExceptionRecord exceptionRecord, ExceptionRecordUpdateCommand command) {
        exceptionRecord.setHandled(false);
        Single<SilkException> silkException$ = silkExceptionRepository.find(command.getException());
        if (exceptionRecord.getSilk() == null) {
            exceptionRecord.setSpindle(command.getSpindle());
            exceptionRecord.setDoffingNum(command.getDoffingNum());
            silkException$ = lineMachineRepository.find(command.getLineMachine()).flatMap(lineMachine -> {
                exceptionRecord.setLineMachine(lineMachine);
                return silkExceptionRepository.find(command.getException());
            });
        }
        return silkException$.flatMap(silkException -> {
            exceptionRecord.setException(silkException);
            return operatorRepository.find(principal);
        }).flatMap(operator -> {
            exceptionRecord.log(operator);
            return exceptionRecordRepository.save(exceptionRecord);
        });
    }

    @Override
    public Single<ExceptionRecord> update(Principal principal, String id, ExceptionRecordUpdateCommand command) {
        return exceptionRecordRepository.find(id).flatMap(it -> save(principal, it, command));
    }

    @Override
    public Completable handle(Principal principal, String id) {
        return exceptionRecordRepository.find(id).flatMap(exceptionRecord -> {
            exceptionRecord.setHandled(true);
            exceptionRecord.setHandleDateTime(new Date());
            return operatorRepository.find(principal).flatMap(operator -> {
                exceptionRecord.setHandler(operator);
                return exceptionRecordRepository.save(exceptionRecord);
            });
        }).ignoreElement();
    }

}
