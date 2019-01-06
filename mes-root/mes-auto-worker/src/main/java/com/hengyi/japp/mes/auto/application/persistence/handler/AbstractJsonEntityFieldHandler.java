package com.hengyi.japp.mes.auto.application.persistence.handler;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;
import com.hengyi.japp.mes.auto.application.persistence.proxy.JsonEntityFieldHandler;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * @author jzb 2018-06-21
 */
@Slf4j
public abstract class AbstractJsonEntityFieldHandler<FIELD> implements JsonEntityFieldHandler {
    protected final Class<? extends JsonEntity> entityClass;
    protected final PropertyDescriptor propertyDescriptor;
    protected final String jsonObjectKey;
    private boolean isReaded;
    private FIELD readValue;
    private boolean isWrited;
    private FIELD writeValue;

    protected AbstractJsonEntityFieldHandler(Class<? extends JsonEntity> entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey) {
        this.entityClass = entityClass;
        this.propertyDescriptor = propertyDescriptor;
        this.jsonObjectKey = jsonObjectKey;
    }

    public synchronized void setReadValue(FIELD readValue) {
        this.readValue = readValue;
        isReaded = true;
    }

    public synchronized void setWriteValue(FIELD writeValue) {
        this.writeValue = writeValue;
        isWrited = true;
    }

    @Override
    public boolean matches(Method method) {
        return isReadMethod(method) || isWriteMethod(method);
    }

    protected boolean isReadMethod(Method method) {
        final String methodName = method.getName();
        final String readMethodName = propertyDescriptor.getReadMethod().getName();
        return StringUtils.equals(methodName, readMethodName);
    }

    protected boolean isWriteMethod(Method method) {
        final String methodName = method.getName();
        final String writeMethodName = propertyDescriptor.getWriteMethod().getName();
        return StringUtils.equals(methodName, writeMethodName);
    }

    @Override
    public Object handleMethod(Object target, Method method, Object[] args, MethodProxy proxy, JsonObject originJsonObject) throws Throwable {
        if (isReadMethod(method)) {
            return handleReadMethod(target, method, args, proxy, originJsonObject);
        } else if (isWriteMethod(method)) {
            return handleWriteMethod(target, method, args, proxy);
        }
        throw new RuntimeException("" + method);
    }

    public Object handleReadMethod(Object target, Method method, Object[] args, MethodProxy proxy, JsonObject originJsonObject) throws Throwable {
        if (isWrited) {
            return writeValue;
        }
        if (isReaded) {
            return readValue;
        }
        final FIELD v = handleReadMethodFromOriginJsonObject(target, method, args, proxy, originJsonObject);
        setReadValue(v);
        return this.readValue;
    }

    protected abstract FIELD handleReadMethodFromOriginJsonObject(Object target, Method method, Object[] args, MethodProxy proxy, JsonObject originJsonObject) throws Throwable;

    private Object handleWriteMethod(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        setWriteValue((FIELD) args[0]);
        proxy.invokeSuper(target, args);
        return null;
    }

    @Override
    public final JsonObject toJsonObject() {
        final JsonObject result = new JsonObject();
        if (isWrited) {
            if (writeValue == null) {
                return result.put(jsonObjectKey, (Object) null);
            }
            return result.put(jsonObjectKey, writeValueToJsonValue(writeValue));
        }
        return result;
    }

    protected Object writeValueToJsonValue(FIELD value) {
        return value;
    }

    @Override
    public String toString() {
        return entityClass + "[" + propertyDescriptor.getName() + "]";
    }
}
