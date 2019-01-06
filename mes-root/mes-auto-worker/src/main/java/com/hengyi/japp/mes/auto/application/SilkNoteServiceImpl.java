package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.SilkNoteUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SilkNote;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkNoteRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class SilkNoteServiceImpl implements SilkNoteService {
    private final SilkNoteRepository silkNoteRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private SilkNoteServiceImpl(SilkNoteRepository silkNoteRepository, OperatorRepository operatorRepository) {
        this.silkNoteRepository = silkNoteRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<SilkNote> create(Principal principal, SilkNoteUpdateCommand command) {
        return silkNoteRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<SilkNote> save(Principal principal, SilkNote silkNote, SilkNoteUpdateCommand command) {
        silkNote.setName(command.getName());
        silkNote.setMustFeedback(command.isMustFeedback());
        return operatorRepository.find(principal).flatMap(operator -> {
            silkNote.log(operator);
            return silkNoteRepository.save(silkNote);
        });
    }

    @Override
    public Single<SilkNote> update(Principal principal, String id, SilkNoteUpdateCommand command) {
        return silkNoteRepository.find(id).flatMap(it -> save(principal, it, command));
    }

}
