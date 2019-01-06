package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.CorporationUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Corporation;
import com.hengyi.japp.mes.auto.repository.CorporationRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class CorporationServiceImpl implements CorporationService {
    private final CorporationRepository corporationRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private CorporationServiceImpl(CorporationRepository corporationRepository, OperatorRepository operatorRepository) {
        this.corporationRepository = corporationRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<Corporation> create(Principal principal, CorporationUpdateCommand command) {
        return corporationRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<Corporation> save(Principal principal, Corporation corporation, CorporationUpdateCommand command) {
        corporation.setCode(command.getCode());
        corporation.setName(command.getName());
        return operatorRepository.find(principal)
                .flatMap(operator -> {
                    corporation.log(operator);
                    return corporationRepository.save(corporation);
                });
    }

    @Override
    public Single<Corporation> update(Principal principal, String id, CorporationUpdateCommand command) {
        return corporationRepository.find(id).flatMap(it -> save(principal, it, command));
    }

}
