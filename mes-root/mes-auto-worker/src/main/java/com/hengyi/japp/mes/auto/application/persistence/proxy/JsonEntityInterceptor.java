package com.hengyi.japp.mes.auto.application.persistence.proxy;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

@Slf4j
public abstract class JsonEntityInterceptor<T extends JsonEntity> implements MethodInterceptor {
    /**
     * 代理的类型会改变，需要创建的时候构造
     */
    @Getter
    protected final Class<T> entityClass;
    @Getter
    protected final Collection<JsonEntityFieldHandler> fieldHandlers;
    /**
     * 已经持久化的json，运行期间不要改写
     */
    protected final JsonObject originJsonObject;

    protected JsonEntityInterceptor(Class<T> entityClass, Collection<JsonEntityFieldHandler> fieldHandlers, JsonObject originJsonObject) {
        this.entityClass = entityClass;
        this.originJsonObject = originJsonObject;
        this.fieldHandlers = fieldHandlers;
    }

    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        final String methodName = method.getName();

        if ("_isNew".equals(methodName)) {
            return originJsonObject.isEmpty();
        }
        if ("_save".equals(methodName)) {
            if (originJsonObject == null || originJsonObject.isEmpty()) {
                return handleCreate();
            } else {
                return handleUpdate();
            }
        }
        if ("_delete".equals(methodName)) {
            if (originJsonObject.isEmpty()) {
                return Completable.complete();
            } else {
                return handleDelete();
            }
        }

        final JsonEntityFieldHandler handler = fieldHandlers.stream()
                .filter(it -> it.matches(method))
                .findFirst()
                .orElse(null);
        if (handler != null) {
            return handler.handleMethod(target, method, args, proxy, originJsonObject);
        }

        if (!Modifier.isAbstract(method.getModifiers())) {
            return proxy.invokeSuper(target, args);
        }

        throw new RuntimeException(method + ", 无法运行");
    }

    protected abstract Single<String> handleCreate();

    protected abstract Single<String> handleUpdate();

    protected abstract Completable handleDelete();

}

