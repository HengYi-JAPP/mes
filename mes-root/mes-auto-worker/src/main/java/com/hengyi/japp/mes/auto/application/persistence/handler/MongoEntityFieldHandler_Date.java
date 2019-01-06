package com.hengyi.japp.mes.auto.application.persistence.handler;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;
import io.vertx.core.json.JsonObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodProxy;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import static com.github.ixtf.japp.core.J.isLongType;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT;

/**
 * @author jzb 2018-06-21
 */
@Slf4j
public class MongoEntityFieldHandler_Date extends AbstractJsonEntityFieldHandler {
    private final SimpleDateFormat df;

    public MongoEntityFieldHandler_Date(Class<? extends JsonEntity> entityClass, PropertyDescriptor propertyDescriptor, String jsonObjectKey) {
        super(entityClass, propertyDescriptor, jsonObjectKey);
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    protected Object handleReadMethodFromOriginJsonObject(Object target, Method method, Object[] args, MethodProxy proxy, JsonObject originJsonObject) throws Throwable {
        return Optional.of(jsonObjectKey)
                .map(originJsonObject::getValue)
                .map(this::toDate)
                .orElse(null);
    }

    @SneakyThrows
    private Date toDate(Object it) {
        if (it instanceof String) {
            return df.parse((String) it);
        }
        if (it instanceof JsonObject) {
            final Object $date = ((JsonObject) it).getValue("$date");
            /**
             * mongo 自己driver，转出来的json里面，日期是 long 类型
             * 基本在懒加载的时候，都是用mongo的client，同步
             */
            if (isLongType($date.getClass())) {
                return new Date((Long) $date);
            }
            final String s = (String) $date;
            return J.nonBlank(s) ? ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.parse(s) : null;
        }
        log.error("Mongo 时间 设置出错：【" + it + "】");
        throw new RuntimeException();
    }

    @Override
    protected Object writeValueToJsonValue(Object value) {
        final Date date = (Date) value;
        return new JsonObject().put("$date", ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(date));
    }

}
