package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.SilkInspectionExceptionUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class SilkServiceImpl implements SilkService {
    private final SilkRepository silkRepository;
    private final SilkExceptionRepository silkExceptionRepository;
    private final GradeRepository gradeRepository;
    private final OperatorRepository operatorRepository;
    private final ExceptionRecordRepository exceptionRecordRepository;

    @Inject
    private SilkServiceImpl(SilkRepository silkRepository, SilkExceptionRepository silkExceptionRepository, GradeRepository gradeRepository, OperatorRepository operatorRepository, ExceptionRecordRepository exceptionRecordRepository) {
        this.silkRepository = silkRepository;
        this.silkExceptionRepository = silkExceptionRepository;
        this.gradeRepository = gradeRepository;
        this.operatorRepository = operatorRepository;
        this.exceptionRecordRepository = exceptionRecordRepository;
    }

    @Override
    public Single<Silk> update(Principal principal, String id, SilkInspectionExceptionUpdateCommand command) {
        return silkRepository.find(id).flatMap(silk -> silkExceptionRepository.find(command.getException()).flatMap(it -> {
            silk.setException(it);
            return gradeRepository.find(command.getGrade());
        }).flatMap(grade -> {
            silk.setGrade(grade);
            return silkRepository.save(silk);
        })).flatMap(silk -> exceptionRecordRepository.findBy(silk).switchIfEmpty(exceptionRecordRepository.create()).flatMap(exceptionRecord -> {
            exceptionRecord.setSilk(silk);
            exceptionRecord.setLineMachine(silk.getLineMachine());
            exceptionRecord.setSpindle(silk.getSpindle());
            exceptionRecord.setDoffingNum(silk.getDoffingNum());
            exceptionRecord.setException(silk.getException());
            exceptionRecord.setHandled(false);
            return operatorRepository.find(principal).flatMap(operator -> {
                exceptionRecord.log(operator);
                return exceptionRecordRepository.save(exceptionRecord);
            });
        }).map(it -> silk));
    }
}
