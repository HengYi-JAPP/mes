package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.WorkshopUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.CorporationRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SapT001lRepository;
import com.hengyi.japp.mes.auto.repository.WorkshopRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class WorkshopServiceImpl implements WorkshopService {
    private final WorkshopRepository workshopRepository;
    private final CorporationRepository corporationRepository;
    private final OperatorRepository operatorRepository;
    private final SapT001lRepository sapT001lRepository;

    @Inject
    private WorkshopServiceImpl(WorkshopRepository workshopRepository, CorporationRepository corporationRepository, OperatorRepository operatorRepository, SapT001lRepository sapT001lRepository) {
        this.workshopRepository = workshopRepository;
        this.corporationRepository = corporationRepository;
        this.operatorRepository = operatorRepository;
        this.sapT001lRepository = sapT001lRepository;
    }

    @Override
    public Single<Workshop> create(Principal principal, WorkshopUpdateCommand command) {
        return workshopRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<Workshop> save(Principal principal, Workshop workshop, WorkshopUpdateCommand command) {
        workshop.setCode(command.getCode());
        workshop.setName(command.getName());
        workshop.setNote(command.getNote());
        return Flowable.fromIterable(J.emptyIfNull(command.getSapT001ls())).map(EntityDTO::getId)
                .flatMapSingle(sapT001lRepository::find).toList()
                .flatMap(sapT001ls -> {
                    workshop.setSapT001ls(sapT001ls);
                    return Flowable.fromIterable(J.emptyIfNull(command.getSapT001lsForeign())).map(EntityDTO::getId)
                            .flatMapSingle(sapT001lRepository::find).toList();
                }).flatMap(sapT001ls -> {
                    workshop.setSapT001lsForeign(sapT001ls);
                    return Flowable.fromIterable(J.emptyIfNull(command.getSapT001lsPallet())).map(EntityDTO::getId)
                            .flatMapSingle(sapT001lRepository::find).toList();
                }).flatMap(sapT001ls -> {
                    workshop.setSapT001lsPallet(sapT001ls);
                    return corporationRepository.find(command.getCorporation().getId());
                }).flatMap(corporation -> {
                    workshop.setCorporation(corporation);
                    return operatorRepository.find(principal);
                }).flatMap(operator -> {
                    workshop.log(operator);
                    return workshopRepository.save(workshop);
                });
    }

    @Override
    public Single<Workshop> update(Principal principal, String id, WorkshopUpdateCommand command) {
        return workshopRepository.find(id).flatMap(it -> save(principal, it, command));
    }

}
