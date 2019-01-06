package com.hengyi.japp.mes.auto.application.persistence.handler;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;

import java.beans.PropertyDescriptor;

/**
 * @author jzb 2018-06-21
 */
public class JsonEntityFieldHandler_CollectionInteger extends JsonEntityFieldHandler_CollectionGeneric<Integer> {

    public JsonEntityFieldHandler_CollectionInteger(Class<? extends JsonEntity> entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey) {
        super(entityClass, propertyDescriptor, jsonObjectKey, Integer.class);
    }
}
