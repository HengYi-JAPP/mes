package com.hengyi.japp.mes.auto.application.persistence.handler;

import java.beans.PropertyDescriptor;

/**
 * @author jzb 2018-06-21
 */
public class JsonEntityFieldHandler_CollectionString extends JsonEntityFieldHandler_CollectionGeneric<String> {

    public JsonEntityFieldHandler_CollectionString(Class entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey) {
        super(entityClass, propertyDescriptor, jsonObjectKey, String.class);
    }

}
