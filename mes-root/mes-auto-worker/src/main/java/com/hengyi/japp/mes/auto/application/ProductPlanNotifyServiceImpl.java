package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.ProductPlanNotifyExeCommand;
import com.hengyi.japp.mes.auto.application.command.ProductPlanNotifyUpdateCommand;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.LineMachineProductPlan;
import com.hengyi.japp.mes.auto.domain.ProductPlanNotify;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class ProductPlanNotifyServiceImpl implements ProductPlanNotifyService {
    private final ApplicationEvents applicationEvents;
    private final ProductPlanNotifyRepository productPlanNotifyRepository;
    private final LineMachineRepository lineMachineRepository;
    private final BatchRepository batchRepository;
    private final LineMachineProductPlanRepository lineMachineProductPlanRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private ProductPlanNotifyServiceImpl(ApplicationEvents applicationEvents, ProductPlanNotifyRepository productPlanNotifyRepository, LineMachineRepository lineMachineRepository, BatchRepository batchRepository, LineMachineProductPlanRepository lineMachineProductPlanRepository, OperatorRepository operatorRepository) {
        this.applicationEvents = applicationEvents;
        this.productPlanNotifyRepository = productPlanNotifyRepository;
        this.lineMachineRepository = lineMachineRepository;
        this.batchRepository = batchRepository;
        this.lineMachineProductPlanRepository = lineMachineProductPlanRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<ProductPlanNotify> create(Principal principal, ProductPlanNotifyUpdateCommand command) {
        return productPlanNotifyRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<ProductPlanNotify> save(Principal principal, ProductPlanNotify productPlanNotify, ProductPlanNotifyUpdateCommand command) {
        productPlanNotify.setType(command.getType());
        productPlanNotify.setName(command.getName());
        productPlanNotify.setStartDate(command.getStartDate());
        return Flowable.fromIterable(command.getLineMachines())
                .map(EntityDTO::getId)
                .distinct()
                .flatMapSingle(lineMachineRepository::find)
                .toList()
                .flatMap(lineMachines -> {
                    productPlanNotify.setLineMachines(lineMachines);
                    return batchRepository.find(command.getBatch().getId());
                })
                .flatMap(batch -> {
                    productPlanNotify.setBatch(batch);
                    return operatorRepository.find(principal);
                })
                .flatMap(operator -> {
                    productPlanNotify.log(operator);
                    return productPlanNotifyRepository.save(productPlanNotify);
                });
    }

    @Override
    public Single<ProductPlanNotify> update(Principal principal, String id, ProductPlanNotifyUpdateCommand command) {
        return productPlanNotifyRepository.find(id).flatMap(it -> save(principal, it, command));
    }

    @Override
    public Completable exe(Principal principal, String id, ProductPlanNotifyExeCommand command) {
        return productPlanNotifyRepository.find(id).flatMapCompletable(productPlanNotify -> lineMachineRepository.find(command.getLineMachine().getId())
                .flatMapCompletable(lineMachine -> exe(principal, productPlanNotify, lineMachine))
        );
    }

    private Completable exe(Principal principal, ProductPlanNotify productPlanNotify, LineMachine lineMachine) {
        final boolean isSameProductPlanNotify = Optional.ofNullable(lineMachine)
                .map(LineMachine::getProductPlan)
                .map(LineMachineProductPlan::getProductPlanNotify)
                .map(productPlanNotify::equals)
                .orElse(false);
        if (isSameProductPlanNotify) {
            return Completable.complete();
        }

        final LineMachineProductPlan prevProductPlan = lineMachine.getProductPlan();
        return lineMachineProductPlanRepository.create().flatMap(productPlan -> operatorRepository.find(principal).flatMap(operator -> {
            productPlan.setStartDate(new Date());
            productPlan.setLineMachine(lineMachine);
            productPlan.setProductPlanNotify(productPlanNotify);
            productPlan.setBatch(productPlanNotify.getBatch());
            productPlan.setPrev(prevProductPlan);
            productPlan.log(operator);
            return lineMachineProductPlanRepository.save(productPlan);
        })).flatMapCompletable(productPlan -> {
            lineMachine.setProductPlan(productPlan);
            final Completable handleLineMachine$ = lineMachineRepository.save(lineMachine).ignoreElement();
            final Completable handlePrevProductPlan$ = prevProductPlan == null ? Completable.complete() : Single
                    .fromCallable(() -> {
                        prevProductPlan.setNext(productPlan);
                        prevProductPlan.setEndDate(productPlan.getStartDate());
                        return prevProductPlan;
                    })
                    .flatMap(lineMachineProductPlanRepository::save)
                    .doAfterSuccess(applicationEvents::fire)
                    .ignoreElement();
            return Completable.mergeArray(handleLineMachine$, handlePrevProductPlan$);
        });
    }

    @Override
    public Completable unExe(Principal principal, String id, ProductPlanNotifyExeCommand command) {
        return productPlanNotifyRepository.find(id).flatMapCompletable(productPlanNotify -> lineMachineRepository.find(command.getLineMachine().getId())
                .flatMapCompletable(lineMachine -> unExe(principal, productPlanNotify, lineMachine))
        );
    }

    private Completable unExe(Principal principal, ProductPlanNotify productPlanNotify, LineMachine lineMachine) {
        return operatorRepository.find(principal).flatMapCompletable(operator -> {
            final LineMachineProductPlan lineMachineProductPlan = lineMachine.getProductPlan();
            if (!Objects.equals(lineMachineProductPlan.getProductPlanNotify(), productPlanNotify)) {
                throw new RuntimeException();
            }
            lineMachineProductPlan.setDeleted(true);
            lineMachineProductPlan.log(operator);
            final Completable lineMachineProductPlanSave$ = lineMachineProductPlanRepository.save(lineMachineProductPlan).ignoreElement();
            final LineMachineProductPlan prevLineMachineProductPlan = lineMachineProductPlan.getPrev();
            lineMachine.setProductPlan(prevLineMachineProductPlan);
            lineMachine.log(operator);
            final Completable lineMachineSave$ = lineMachineRepository.save(lineMachine).ignoreElement();
            return Completable.mergeArray(lineMachineProductPlanSave$, lineMachineSave$);
        });
    }

    @Override
    public Completable finish(Principal principal, String id) {
        return productPlanNotifyRepository.find(id).flatMap(productPlanNotify -> operatorRepository.find(principal).flatMap(operator -> {
            productPlanNotify.setEndDate(new Date());
            productPlanNotify.log(operator);
            return productPlanNotifyRepository.save(productPlanNotify);
        })).ignoreElement();
    }

    @Override
    public Completable unFinish(Principal principal, String id) {
        return productPlanNotifyRepository.find(id).flatMap(productPlanNotify -> operatorRepository.find(principal).flatMap(operator -> {
            productPlanNotify.setEndDate(null);
            productPlanNotify.log(operator);
            return productPlanNotifyRepository.save(productPlanNotify);
        })).ignoreElement();
    }

}
