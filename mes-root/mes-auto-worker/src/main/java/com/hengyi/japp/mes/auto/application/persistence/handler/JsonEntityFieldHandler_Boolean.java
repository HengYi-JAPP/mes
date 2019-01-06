package com.hengyi.japp.mes.auto.application.persistence.handler;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;
import io.vertx.core.json.JsonObject;
import net.sf.cglib.proxy.MethodProxy;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * @author jzb 2018-06-21
 */
public class JsonEntityFieldHandler_Boolean extends AbstractJsonEntityFieldHandler<Boolean> {

    public JsonEntityFieldHandler_Boolean(Class<? extends JsonEntity> entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey) {
        super(entityClass, propertyDescriptor, jsonObjectKey);
    }

    @Override
    protected Boolean handleReadMethodFromOriginJsonObject(Object target, Method method, Object[] args, MethodProxy proxy, JsonObject originJsonObject) throws Throwable {
        return originJsonObject.getBoolean(jsonObjectKey, false);
    }

}
