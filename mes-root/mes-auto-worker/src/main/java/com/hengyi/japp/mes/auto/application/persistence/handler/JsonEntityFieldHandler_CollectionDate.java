package com.hengyi.japp.mes.auto.application.persistence.handler;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;

import java.beans.PropertyDescriptor;
import java.util.Date;

/**
 * @author jzb 2018-06-21
 */
public class JsonEntityFieldHandler_CollectionDate extends JsonEntityFieldHandler_CollectionGeneric<Date> {

    public JsonEntityFieldHandler_CollectionDate(Class<? extends JsonEntity> entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey) {
        super(entityClass, propertyDescriptor, jsonObjectKey, Date.class);
    }

//    public JsonEntityFieldHandler_CollectionDate(JsonEntityManager mongoEntiyManager, Class<? extends JsonEntity> entityClass, JsonObject originMongoJsonObject, PropertyDescriptor propertyDescriptor) {
//        super(mongoEntiyManager, entityClass, Date.class, originMongoJsonObject, propertyDescriptor);
//    }
//
//    @Override
//    protected Collection<Date> convertJsonArray(JsonArray jsonArray) {
//        return Flowable.fromIterable(jsonArray)
//                .cast(Long.class)
//                .map(Date::new)
//                .toList()
//                .observeOn(Schedulers.io())
//                .subscribeOn(Schedulers.io())
//                .blockingGet();
//    }
//
//    @Override
//    protected Long convertJsonArrayItem(Date date) {
//        return date.getTime();
//    }

}
