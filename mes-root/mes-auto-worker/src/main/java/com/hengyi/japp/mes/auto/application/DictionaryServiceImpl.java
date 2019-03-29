package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.DictionaryUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Dictionary;
import com.hengyi.japp.mes.auto.repository.DictionaryRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author liuyuan
 * @create 2019-03-14 14:43
 * @description
 **/
@Slf4j
@Singleton
public class DictionaryServiceImpl implements DictionaryService {
    private final DictionaryRepository dictionaryRepository;

    @Inject
    private DictionaryServiceImpl(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    @Override
    public Single<Dictionary> create(Principal principal, DictionaryUpdateCommand command) {
        return dictionaryRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<Dictionary> save(Principal principal, Dictionary dictionary, DictionaryUpdateCommand command) {
        log.info(principal.getName());
        dictionary.setKey(command.getKey());
        dictionary.setValue(command.getValue());
        return dictionaryRepository.save(dictionary);
    }

    @Override
    public Single<Dictionary> update(Principal principal, String id, DictionaryUpdateCommand command) {
        return dictionaryRepository.find(id).flatMap(it -> save(principal, it, command));
    }

    @Override
    public Flowable<Dictionary> getByKey(String key) {
        return dictionaryRepository.findByKey(key);
    }


    @Override
    public Completable delete(String id) {
        return dictionaryRepository.delete(id);
    }
}
