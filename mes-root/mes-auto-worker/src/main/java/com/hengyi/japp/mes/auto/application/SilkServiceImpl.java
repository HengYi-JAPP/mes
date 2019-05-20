package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.SilkInspectionExceptionUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.repository.GradeRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkExceptionRepository;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class SilkServiceImpl implements SilkService {
    private final ApplicationEvents applicationEvents;
    private final SilkRepository silkRepository;
    private final SilkExceptionRepository silkExceptionRepository;
    private final GradeRepository gradeRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private SilkServiceImpl(ApplicationEvents applicationEvents, SilkRepository silkRepository, SilkExceptionRepository silkExceptionRepository, GradeRepository gradeRepository, OperatorRepository operatorRepository) {
        this.applicationEvents = applicationEvents;
        this.silkRepository = silkRepository;
        this.silkExceptionRepository = silkExceptionRepository;
        this.gradeRepository = gradeRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<Silk> update(Principal principal, String id, SilkInspectionExceptionUpdateCommand command) {
        return silkRepository.find(id).flatMap(silk -> silkExceptionRepository.find(command.getException().getId()).flatMap(it -> {
            silk.setException(it);
            return gradeRepository.find(command.getGrade().getId()).flatMap(grade -> {
                silk.setGrade(grade);
                return  operatorRepository.find(principal).flatMap(operator -> {
                        applicationEvents.fire(silk,operator);
                    return silkRepository.save(silk);
                 });
            });
        }));
    }
}