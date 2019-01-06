package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.GradeUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.repository.GradeRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class GradeServiceImpl implements GradeService {
    private final GradeRepository gradeRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private GradeServiceImpl(GradeRepository gradeRepository, OperatorRepository operatorRepository) {
        this.gradeRepository = gradeRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<Grade> create(Principal principal, GradeUpdateCommand command) {
        return gradeRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<Grade> save(Principal principal, Grade grade, GradeUpdateCommand command) {
        grade.setName(command.getName());
        grade.setCode(command.getCode());
        grade.setSortBy(command.getSortBy());
        return operatorRepository.find(principal)
                .flatMap(operator -> {
                    grade.log(operator);
                    return gradeRepository.save(grade);
                });
    }

    @Override
    public Single<Grade> update(Principal principal, String id, GradeUpdateCommand command) {
        return gradeRepository.find(id).flatMap(it -> save(principal, it, command));
    }
}
