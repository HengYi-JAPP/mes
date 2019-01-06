package com.hengyi.japp.mes.auto.application.persistence.handler;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;
import com.hengyi.japp.mes.auto.application.persistence.MongoEntity;
import com.hengyi.japp.mes.auto.application.persistence.proxy.JsonEntityManager;
import io.vertx.core.json.JsonObject;
import net.sf.cglib.proxy.MethodProxy;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author jzb 2018-06-21
 */
public class MongoEntityFieldHandler_Entity<FIELD extends MongoEntity> extends AbstractJsonEntityFieldHandler<FIELD> {
    private final JsonEntityManager<FIELD> mongoEntityManager;
    private final Class<FIELD> targetClass;

    public MongoEntityFieldHandler_Entity(Class<? extends JsonEntity> entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey, JsonEntityManager<FIELD> mongoEntityManager) {
        super(entityClass, propertyDescriptor, jsonObjectKey);
        targetClass = (Class<FIELD>) propertyDescriptor.getPropertyType();
        this.mongoEntityManager = mongoEntityManager;
    }

    @Override
    protected FIELD handleReadMethodFromOriginJsonObject(Object target, Method method, Object[] args, MethodProxy proxy, JsonObject originJsonObject) throws Throwable {
        return Optional.ofNullable(originJsonObject.getString(jsonObjectKey, null))
                .map(id -> mongoEntityManager.find(targetClass, id))
                .orElse(null);
    }

    @Override
    protected Object writeValueToJsonValue(FIELD value) {
        return value.getId();
    }

}
