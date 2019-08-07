package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.SilkCarRecordDestinationUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SilkCarRecordDestination;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkCarRecordDestinationRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class SilkCarRecordDestinationServiceImpl implements SilkCarRecordDestinationService {
    private final SilkCarRecordDestinationRepository silkCarRecordDestinationRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private SilkCarRecordDestinationServiceImpl(SilkCarRecordDestinationRepository silkCarRecordDestinationRepository, OperatorRepository operatorRepository) {
        this.silkCarRecordDestinationRepository = silkCarRecordDestinationRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<SilkCarRecordDestination> create(Principal principal, SilkCarRecordDestinationUpdateCommand command) {
        return silkCarRecordDestinationRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<SilkCarRecordDestination> save(Principal principal, SilkCarRecordDestination silkCarRecordDestination, SilkCarRecordDestinationUpdateCommand command) {
        silkCarRecordDestination.setName(command.getName());
        return operatorRepository.find(principal)
                .flatMap(operator -> {
                    silkCarRecordDestination.log(operator);
                    return silkCarRecordDestinationRepository.save(silkCarRecordDestination);
                });
    }

    @Override
    public Single<SilkCarRecordDestination> update(Principal principal, String id, SilkCarRecordDestinationUpdateCommand command) {
        return silkCarRecordDestinationRepository.find(id).flatMap(it -> save(principal, it, command));
    }

}
