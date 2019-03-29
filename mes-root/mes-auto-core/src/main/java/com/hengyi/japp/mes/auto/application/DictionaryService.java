package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.DictionaryUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Dictionary;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author liuyuan
 * @create 2019-03-14 14:05
 * @description
 **/
public interface DictionaryService {
    Single<Dictionary> create(Principal principal, DictionaryUpdateCommand command);

    Single<Dictionary> update(Principal principal, String id, DictionaryUpdateCommand command);

    Flowable<Dictionary> getByKey(String key);

    Completable delete(String id);
}
