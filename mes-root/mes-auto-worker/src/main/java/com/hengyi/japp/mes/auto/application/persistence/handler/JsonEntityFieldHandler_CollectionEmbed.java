package com.hengyi.japp.mes.auto.application.persistence.handler;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;

import java.beans.PropertyDescriptor;

/**
 * @author jzb 2018-06-21
 */
public class JsonEntityFieldHandler_CollectionEmbed<T extends Object> extends JsonEntityFieldHandler_CollectionGeneric<T> {

    public JsonEntityFieldHandler_CollectionEmbed(Class<? extends JsonEntity> entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey, Class<T> targetClass) {
        super(entityClass, propertyDescriptor, jsonObjectKey, targetClass);
    }

//    public JsonEntityFieldHandler_CollectionEmbed(JsonEntityManager jsonEntityManager, Class<? extends JsonEntity> entityClass, Class<T> targetClass, JsonObject originMongoJsonObject, PropertyDescriptor propertyDescriptor) {
//        super(jsonEntityManager, entityClass, targetClass, originMongoJsonObject, propertyDescriptor);
//    }
//
//    @Override
//    protected Collection<T> convertJsonArray(JsonArray jsonArray) {
//        return Flowable.fromIterable(jsonArray)
//                .cast(JsonObject.class)
//                .map(it-> it.mapTo(targetClass))
//                .toList()
//                .observeOn(Schedulers.io())
//                .subscribeOn(Schedulers.io())
//                .blockingGet();
//    }
//
//    @Override
//    protected Object convertJsonArrayItem(T t) {
//        return JsonObject.mapFrom(t);
//    }
}
