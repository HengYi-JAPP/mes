package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.DictionaryQuery;
import com.hengyi.japp.mes.auto.domain.Dictionary;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author liuyuan
 * @create 2019-03-14 14:20
 * @description
 **/
public interface DictionaryRepository {
    Single<Dictionary> create();

    Single<Dictionary> find(String id);

    Single<Dictionary> save(Dictionary dictionary);

    Flowable<Dictionary> findByKey(String key);

    Flowable<Dictionary> list();

    Single<DictionaryQuery.Result> query(DictionaryQuery query);
}
