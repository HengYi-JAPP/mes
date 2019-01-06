package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.TemporaryBoxUpdateCommand;
import com.hengyi.japp.mes.auto.domain.TemporaryBox;
import com.hengyi.japp.mes.auto.repository.BatchRepository;
import com.hengyi.japp.mes.auto.repository.GradeRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.TemporaryBoxRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class TemporaryBoxServiceImpl implements TemporaryBoxService {
    private final TemporaryBoxRepository temporaryBoxRepository;
    private final BatchRepository batchRepository;
    private final GradeRepository gradeRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private TemporaryBoxServiceImpl(TemporaryBoxRepository temporaryBoxRepository, BatchRepository batchRepository, GradeRepository gradeRepository, OperatorRepository operatorRepository) {
        this.temporaryBoxRepository = temporaryBoxRepository;
        this.batchRepository = batchRepository;
        this.gradeRepository = gradeRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<TemporaryBox> create(Principal principal, TemporaryBoxUpdateCommand command) {
        return temporaryBoxRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<TemporaryBox> save(Principal principal, TemporaryBox temporaryBox, TemporaryBoxUpdateCommand command) {
        temporaryBox.setCode(command.getCode());
        return batchRepository.find(command.getBatch().getId()).flatMap(batch -> {
            temporaryBox.setBatch(batch);
            return gradeRepository.find(command.getGrade().getId());
        }).flatMap(grade -> {
            temporaryBox.setGrade(grade);
            return operatorRepository.find(principal);
        }).flatMap(operator -> {
            temporaryBox.log(operator);
            return temporaryBoxRepository.save(temporaryBox);
        });
    }

    @Override
    public Single<TemporaryBox> update(Principal principal, String id, TemporaryBoxUpdateCommand command) {
        return temporaryBoxRepository.find(id).flatMap(it -> save(principal, it, command));
    }

//    @Override
//    public Completable increment(Principal principal, TemporaryBoxIncrementEvent event) {
//        return temporaryBoxRepository.findByCode(event.getTemporaryBox().getCode())
//                .flatMapCompletable(temporaryBox -> getSilks(event)
//                        .flatMapSingle(silk -> {
//                            silk.setTemporaryBox(temporaryBox);
//                            return silkRepository.save(silk);
//                        }).toList()
//                        .flatMapCompletable(it -> temporaryBoxRepository.rxInc(temporaryBox.getId(), event.getSilkRuntimes().size()))
//                        .andThen(silkCarRuntimeService.addEventSource(event.getSilkCarRecord(), event))
//                );
//    }
//
//    private Flowable<Silk> getSilks(TemporaryBoxIncrementEvent event) {
//        return silkCarRepository.find(event.getSilkCarRecord().getSilkCar().getId())
//                .map(SilkCar::getCode)
//                .flatMap(silkCarRuntimeService::findByCode)
//                .flatMapPublisher(silkCarRuntime -> {
//                    final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
//                    if (!Objects.equals(silkCarRecord.getId(), event.getSilkCarRecord().getId())) {
//                        throw new SilkCarStatusException();
//                    }
//                    final Map<String, Silk> map = silkCarRuntime.getSilkRuntimes()
//                            .stream()
//                            .map(SilkRuntime::getSilk)
//                            .collect(Collectors.toMap(it -> it.getId(), Function.identity()));
//                    return Flowable.fromIterable(event.getSilkRuntimes())
//                            .map(SilkRuntimeDTO::getSilk)
//                            .map(EntityDTO::getId)
//                            .map(map::get);
//                });
//    }
//
//    @Override
//    public Completable decrement(Principal principal, TemporaryBoxDecrementEvent event) {
//        return temporaryBoxRepository.findByCode(event.getTemporaryBox().getCode())
//                .flatMapCompletable(temporaryBox -> {
//                    final String id = temporaryBox.getId();
//                    return temporaryBoxRepository.rxInc(id, event.getCount() * -1);
//                });
//    }
}
