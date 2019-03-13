package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.SapT001lUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SapT001l;
import com.hengyi.japp.mes.auto.exception.JJsonEntityNotExsitException;
import com.hengyi.japp.mes.auto.repository.SapT001lRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class SapT001lServiceImpl implements SapT001lService {
    private final SapT001lRepository sapT001lRepository;

    @Inject
    private SapT001lServiceImpl(SapT001lRepository sapT001lRepository) {
        this.sapT001lRepository = sapT001lRepository;
    }

    @Override
    public Single<SapT001l> create(SapT001lUpdateCommand command) {
        return sapT001lRepository.find(command.getLgort())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof NoSuchElementException) {
                        return fetch(command);
                    }
                    throw new RuntimeException(ex);
                })
                .flatMap(sapT001l -> {
                    sapT001l.setLgort(command.getLgort());
                    sapT001l.setLgobe(command.getLgobe());
                    return sapT001lRepository.save(sapT001l);
                });
    }

    private Single<SapT001l> fetch(SapT001lUpdateCommand command) {
        return sapT001lRepository.create().flatMap(sapT001l -> {
            sapT001l.setLgort(command.getLgort());
            sapT001l.setLgobe(command.getLgobe());
            return sapT001lRepository.save(sapT001l);
        });
    }

}
