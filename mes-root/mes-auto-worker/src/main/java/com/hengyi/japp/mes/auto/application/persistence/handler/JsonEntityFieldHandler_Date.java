package com.hengyi.japp.mes.auto.application.persistence.handler;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;
import io.vertx.core.json.JsonObject;
import net.sf.cglib.proxy.MethodProxy;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * @author jzb 2018-06-21
 */
public class JsonEntityFieldHandler_Date extends AbstractJsonEntityFieldHandler<Date> {

    public JsonEntityFieldHandler_Date(Class<? extends JsonEntity> entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey) {
        super(entityClass, propertyDescriptor, jsonObjectKey);
    }

    @Override
    protected Date handleReadMethodFromOriginJsonObject(Object target, Method method, Object[] args, MethodProxy proxy, JsonObject originJsonObject) throws Throwable {
        final Long l = originJsonObject.getLong(jsonObjectKey, null);
        return l == null ? null : new Date(l);
    }

}
