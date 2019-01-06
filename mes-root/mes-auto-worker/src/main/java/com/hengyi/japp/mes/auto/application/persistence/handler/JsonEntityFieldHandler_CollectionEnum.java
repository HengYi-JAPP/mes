package com.hengyi.japp.mes.auto.application.persistence.handler;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;

import java.beans.PropertyDescriptor;

/**
 * @author jzb 2018-06-21
 */
public class JsonEntityFieldHandler_CollectionEnum<T extends Enum> extends JsonEntityFieldHandler_CollectionGeneric<T> {

    public JsonEntityFieldHandler_CollectionEnum(Class<? extends JsonEntity> entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey, Class<T> targetClass) {
        super(entityClass, propertyDescriptor, jsonObjectKey, targetClass);
    }

    @Override
    protected T toResultItem(Object o) {
        final String value = (String) o;
        return (T) Enum.valueOf(targetClass, value);
    }

    @Override
    protected Object toJsonArrayItem(T t) {
        return t.name();
    }
}
