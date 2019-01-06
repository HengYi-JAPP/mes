package com.hengyi.japp.mes.auto.application.persistence.proxy;

import io.vertx.core.json.JsonObject;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author jzb 2018-06-29
 */
public interface JsonEntityFieldHandler {
    boolean matches(Method method);

    Object handleMethod(Object target, Method method, Object[] args, MethodProxy proxy, JsonObject originJsonObject) throws Throwable;

    /**
     * @return 保存的数据格式
     */
    JsonObject toJsonObject();
}
