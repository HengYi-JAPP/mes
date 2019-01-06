package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.SilkExceptionUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SilkException;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkExceptionRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class SilkExceptionServiceImpl implements SilkExceptionService {
    private final SilkExceptionRepository silkExceptionRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private SilkExceptionServiceImpl(SilkExceptionRepository silkExceptionRepository, OperatorRepository operatorRepository) {
        this.silkExceptionRepository = silkExceptionRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<SilkException> create(Principal principal, SilkExceptionUpdateCommand command) {
        return silkExceptionRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<SilkException> save(Principal principal, SilkException silkException, SilkExceptionUpdateCommand command) {
        silkException.setName(command.getName());
        return operatorRepository.find(principal)
                .flatMap(operator -> {
                    silkException.log(operator);
                    return silkExceptionRepository.save(silkException);
                });
    }

    @Override
    public Single<SilkException> update(Principal principal, String id, SilkExceptionUpdateCommand command) {
        return silkExceptionRepository.find(id).flatMap(it -> save(principal, it, command));
    }

}
