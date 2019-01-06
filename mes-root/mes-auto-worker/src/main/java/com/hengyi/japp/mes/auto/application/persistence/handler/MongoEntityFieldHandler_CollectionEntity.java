package com.hengyi.japp.mes.auto.application.persistence.handler;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;
import com.hengyi.japp.mes.auto.application.persistence.MongoEntity;
import com.hengyi.japp.mes.auto.application.persistence.proxy.JsonEntityManager;

import java.beans.PropertyDescriptor;

/**
 * @author jzb 2018-06-21
 */
public class MongoEntityFieldHandler_CollectionEntity<T extends MongoEntity> extends JsonEntityFieldHandler_CollectionGeneric<T> {
    private final JsonEntityManager<T> mongoEntityManager;

    public MongoEntityFieldHandler_CollectionEntity(Class<? extends JsonEntity> entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey, Class<T> targetClass, JsonEntityManager mongoEntityManager) {
        super(entityClass, propertyDescriptor, jsonObjectKey, targetClass);
        this.mongoEntityManager = mongoEntityManager;
    }

    @Override
    protected T toResultItem(Object o) {
        final String id = (String) o;
        return mongoEntityManager.find(targetClass, id);
    }

    @Override
    protected Object toJsonArrayItem(T t) {
        return t.getId();
    }

}
