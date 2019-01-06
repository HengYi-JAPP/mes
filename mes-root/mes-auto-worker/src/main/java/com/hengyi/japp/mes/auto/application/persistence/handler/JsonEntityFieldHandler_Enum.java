package com.hengyi.japp.mes.auto.application.persistence.handler;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;
import io.vertx.core.json.JsonObject;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * @author jzb 2018-06-21
 */
public class JsonEntityFieldHandler_Enum<T extends Enum> extends AbstractJsonEntityFieldHandler<Enum> {
    private final Class<T> propertyType;

    public JsonEntityFieldHandler_Enum(Class<? extends JsonEntity> entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey) {
        super(entityClass, propertyDescriptor, jsonObjectKey);
        propertyType = (Class<T>) propertyDescriptor.getPropertyType();
    }

    @Override
    protected Enum handleReadMethodFromOriginJsonObject(Object target, Method method, Object[] args, MethodProxy proxy, JsonObject originJsonObject) throws Throwable {
        final String s = originJsonObject.getString(jsonObjectKey, null);
        return StringUtils.isNotBlank(s) ? Enum.valueOf(propertyType, s) : null;
    }

    @Override
    protected Object writeValueToJsonValue(Enum value) {
        return value.name();
    }
}
