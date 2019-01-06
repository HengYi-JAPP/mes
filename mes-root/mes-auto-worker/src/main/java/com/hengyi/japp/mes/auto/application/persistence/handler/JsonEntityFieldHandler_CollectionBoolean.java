package com.hengyi.japp.mes.auto.application.persistence.handler;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;

import java.beans.PropertyDescriptor;

/**
 * @author jzb 2018-06-21
 */
public class JsonEntityFieldHandler_CollectionBoolean extends JsonEntityFieldHandler_CollectionGeneric<Boolean> {

    public JsonEntityFieldHandler_CollectionBoolean(Class<? extends JsonEntity> entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey) {
        super(entityClass, propertyDescriptor, jsonObjectKey, Boolean.class);
    }

}
