package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.SilkCarUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkCarRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class SilkCarServiceImpl implements SilkCarService {
    private final SilkCarRepository silkCarRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private SilkCarServiceImpl(SilkCarRepository silkCarRepository, OperatorRepository operatorRepository) {
        this.silkCarRepository = silkCarRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<SilkCar> create(Principal principal, SilkCarUpdateCommand command) {
        return silkCarRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<SilkCar> save(Principal principal, SilkCar silkCar, SilkCarUpdateCommand command) {
        silkCarRepository.findByCode(command.getCode());

        silkCar.setNumber(command.getNumber());
        silkCar.setCode(command.getCode());
        silkCar.setRow(command.getRow());
        silkCar.setCol(command.getCol());
        silkCar.setType(command.getType());
        return operatorRepository.find(principal)
                .flatMap(operator -> {
                    silkCar.log(operator);
                    return silkCarRepository.save(silkCar);
                });
    }

    @Override
    public Single<SilkCar> update(Principal principal, String id, SilkCarUpdateCommand command) {
        return silkCarRepository.find(id).flatMap(it -> save(principal, it, command));
    }
}
