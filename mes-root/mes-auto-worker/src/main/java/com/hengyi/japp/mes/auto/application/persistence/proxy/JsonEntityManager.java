package com.hengyi.japp.mes.auto.application.persistence.proxy;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-21
 */
public interface JsonEntityManager<T extends JsonEntity> {

    <E extends T> E find(Class<E> entityClass, String id);

    <E extends T> Single<E> rxFind(Class<E> entityClass, String id);

    <E extends T> Flowable<E> rxFindAll(Class<E> entityClass);

}
