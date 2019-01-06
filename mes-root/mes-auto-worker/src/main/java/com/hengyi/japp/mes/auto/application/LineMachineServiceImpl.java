package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.LineMachineUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.LineMachineProductPlan;
import com.hengyi.japp.mes.auto.repository.LineMachineProductPlanRepository;
import com.hengyi.japp.mes.auto.repository.LineMachineRepository;
import com.hengyi.japp.mes.auto.repository.LineRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.List;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class LineMachineServiceImpl implements LineMachineService {
    private final LineMachineRepository lineMachineRepository;
    private final OperatorRepository operatorRepository;
    private final LineRepository lineRepository;
    private final LineMachineProductPlanRepository lineMachineProductPlanRepository;

    @Inject
    private LineMachineServiceImpl(LineMachineRepository lineMachineRepository, OperatorRepository operatorRepository, LineRepository lineRepository, LineMachineProductPlanRepository lineMachineProductPlanRepository) {
        this.lineMachineRepository = lineMachineRepository;
        this.operatorRepository = operatorRepository;
        this.lineRepository = lineRepository;
        this.lineMachineProductPlanRepository = lineMachineProductPlanRepository;
    }

    @Override
    public Single<LineMachine> create(Principal principal, LineMachineUpdateCommand command) {
        return lineRepository.find(command.getLine().getId()).flatMapPublisher(Line::lineMachines)
                .filter(it -> it.getItem() == command.getItem()).toList()
                .flatMap(lineMachines -> {
                    if (J.isEmpty(lineMachines)) {
                        return lineMachineRepository.create().flatMap(it -> save(principal, it, command));
                    }
                    return Single.just(lineMachines.get(0));
                });
    }

    private Single<LineMachine> save(Principal principal, LineMachine lineMachine, LineMachineUpdateCommand command) {
        lineMachine.setItem(command.getItem());
        lineMachine.setSpindleNum(command.getSpindleNum());
        lineMachine.setSpindleSeq(command.getSpindleSeq());
        return lineRepository.find(command.getLine().getId()).flatMap(line -> {
            lineMachine.setLine(line);
            return operatorRepository.find(principal);
        }).flatMap(operator -> {
            lineMachine.log(operator);
            return lineMachineRepository.save(lineMachine);
        });
    }

    @Override
    public Single<LineMachine> update(Principal principal, String id, LineMachineUpdateCommand command) {
        return lineMachineRepository.find(id).flatMap(it -> save(principal, it, command));
    }

    @Override
    public Flowable<LineMachineProductPlan> listTimeline(String id, String currentId, int size) {
        if (J.isBlank(currentId)) {
            return lineMachineRepository.find(id).map(LineMachine::getProductPlan).flatMapPublisher(it -> listTimeline(it, size));
        }
        return lineMachineProductPlanRepository.find(currentId).flatMapPublisher(it -> listTimeline(it, size));
    }

    private Flowable<LineMachineProductPlan> listTimeline(LineMachineProductPlan current, int size) {
        final List<LineMachineProductPlan> list = Lists.newArrayList(current);
        LineMachineProductPlan prev = current;
        for (int i = 0; i < size; i++) {
            prev = prev.getPrev();
            if (prev == null) {
                break;
            }
            list.add(prev);
        }
        return Flowable.fromIterable(list);
    }
}
