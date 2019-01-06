package com.hengyi.japp.mes.auto.application.persistence.handler;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author jzb 2018-06-21
 */
public abstract class JsonEntityFieldHandler_CollectionGeneric<T> extends AbstractJsonEntityFieldHandler<Collection<T>> {
    protected final Class<T> targetClass;

    protected JsonEntityFieldHandler_CollectionGeneric(Class<? extends JsonEntity> entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey, Class<T> targetClass) {
        super(entityClass, propertyDescriptor, jsonObjectKey);
        this.targetClass = targetClass;
    }

    @Override
    protected Collection<T> handleReadMethodFromOriginJsonObject(Object target, Method method, Object[] args, MethodProxy proxy, JsonObject originJsonObject) throws Throwable {
        return Optional.ofNullable(originJsonObject.getJsonArray(jsonObjectKey, null))
                .map(this::convertJsonArray)
                .orElse(null);
    }

    protected Collection<T> convertJsonArray(JsonArray jsonArray) {
        return StreamSupport.stream(IterableUtils.emptyIfNull(jsonArray).spliterator(), false)
                .filter(Objects::nonNull)
                .map(it -> this.toResultItem(it))
                .collect(Collectors.toList());
    }

    protected T toResultItem(Object o) {
        return (T) o;
    }

    @Override
    protected Object writeValueToJsonValue(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return null;
        }
        final JsonArray result = new JsonArray();
        collection.forEach(it -> {
            final Object item = toJsonArrayItem(it);
            result.add(item);
        });
        return result;
    }

    protected Object toJsonArrayItem(T t) {
        return t;
    }

}
