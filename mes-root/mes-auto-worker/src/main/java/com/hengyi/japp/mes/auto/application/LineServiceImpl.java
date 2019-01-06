package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.LineUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.repository.LineRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.WorkshopRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class LineServiceImpl implements LineService {
    private final LineRepository lineRepository;
    private final WorkshopRepository workshopRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private LineServiceImpl(LineRepository lineRepository, WorkshopRepository workshopRepository, OperatorRepository operatorRepository) {
        this.lineRepository = lineRepository;
        this.workshopRepository = workshopRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<Line> create(Principal principal, LineUpdateCommand command) {
        return lineRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<Line> save(Principal principal, Line line, LineUpdateCommand command) {
        line.setName(command.getName());
        line.setDoffingType(command.getDoffingType());
        return workshopRepository.find(command.getWorkshop().getId())
                .flatMap(workshop -> {
                    line.setWorkshop(workshop);
                    return operatorRepository.find(principal);
                })
                .flatMap(operator -> {
                    line.log(operator);
                    return lineRepository.save(line);
                });
    }

    @Override
    public Single<Line> update(Principal principal, String id, LineUpdateCommand command) {
        return lineRepository.find(id)
                .flatMap(it -> save(principal, it, command));
    }

    @Override
    public Completable delete(Principal principal, String id) {
        return lineRepository.find(id)
                .flatMap(line -> operatorRepository.find(principal)
                        .flatMap(operator -> {
                            line.setDeleted(true);
                            line.log(operator);
                            return lineRepository.save(line);
                        })
                )
                .ignoreElement();
    }
}
