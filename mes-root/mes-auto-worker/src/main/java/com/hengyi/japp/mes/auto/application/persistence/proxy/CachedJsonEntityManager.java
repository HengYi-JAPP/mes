package com.hengyi.japp.mes.auto.application.persistence.proxy;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;

/**
 * @author jzb 2018-07-25
 */
public interface CachedJsonEntityManager<T extends JsonEntity> extends JsonEntityManager<T> {
    <E extends T> void refresh(Class<E> entityClass, String id);
}
