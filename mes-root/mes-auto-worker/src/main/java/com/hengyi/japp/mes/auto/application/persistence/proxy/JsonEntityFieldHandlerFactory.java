package com.hengyi.japp.mes.auto.application.persistence.proxy;

import com.hengyi.japp.mes.auto.Util;
import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;
import com.hengyi.japp.mes.auto.application.persistence.MongoEntity;
import com.hengyi.japp.mes.auto.application.persistence.handler.*;
import org.apache.commons.beanutils.PropertyUtils;

import javax.persistence.Transient;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.ixtf.japp.core.J.*;

/**
 * @author jzb 2018-07-04
 */
public class JsonEntityFieldHandlerFactory {


    public static Collection<JsonEntityFieldHandler> get(JsonEntityManager jsonEntityManager, Class<? extends JsonEntity> entityClass) {
        return Arrays.stream(PropertyUtils.getPropertyDescriptors(entityClass))
                // id 需要特殊处理，在mongo里面全部用默认主键的key _id，
                .filter(it -> !"id".equals(it.getName()))
                .filter(it -> Objects.nonNull(it.getReadMethod()))
                .filter(it -> Objects.nonNull(it.getWriteMethod()))
                .filter(it -> !"class".equalsIgnoreCase(it.getName()))
                .filter(it -> {
                    final String fieldName = it.getName();
//                    entityClass.getDeclaredField(fieldName)
                    final Class<?> propertyType = it.getPropertyType();
                    final Transient aTransient = propertyType.getAnnotation(Transient.class);
                    return aTransient == null;
                })
                // cglib 特有
                .filter(it -> !"callbacks".equals(it.getName()))
                .map(propertyDescriptor -> {
                    final Class<?> propertyType = propertyDescriptor.getPropertyType();
                    final String jsonObjectKey = MongoUtil.jsonObjectKey(entityClass, propertyDescriptor);

                    if (propertyType == String.class) {
                        return new JsonEntityFieldHandler_String(entityClass, propertyDescriptor, jsonObjectKey);
                    }

                    if (isBooleanType(propertyType)) {
                        return new JsonEntityFieldHandler_Boolean(entityClass, propertyDescriptor, jsonObjectKey);
                    }

                    if (isIntegerType(propertyType)) {
                        return new JsonEntityFieldHandler_Integer(entityClass, propertyDescriptor, jsonObjectKey);
                    }

                    if (isLongType(propertyType)) {
                        return new JsonEntityFieldHandler_Long(entityClass, propertyDescriptor, jsonObjectKey);
                    }

                    if (isFloatType(propertyType)) {
                        return new JsonEntityFieldHandler_Float(entityClass, propertyDescriptor, jsonObjectKey);
                    }

                    if (isDoubleType(propertyType)) {
                        return new JsonEntityFieldHandler_Double(entityClass, propertyDescriptor, jsonObjectKey);
                    }

                    if (Enum.class.isAssignableFrom(propertyType)) {
                        return new JsonEntityFieldHandler_Enum(entityClass, propertyDescriptor, jsonObjectKey);
                    }

                    if (Date.class.isAssignableFrom(propertyType)) {
                        if (MongoEntity.class.isAssignableFrom(entityClass)) {
                            return new MongoEntityFieldHandler_Date(entityClass, propertyDescriptor, jsonObjectKey);
                        }
                        return new JsonEntityFieldHandler_Date(entityClass, propertyDescriptor, jsonObjectKey);
                    }

                    if (MongoEntity.class.isAssignableFrom(propertyType)) {
                        return new MongoEntityFieldHandler_Entity(entityClass, propertyDescriptor, jsonObjectKey, jsonEntityManager);
                    }

                    if (Collection.class.isAssignableFrom(propertyType)) {
                        final Class<?> targetClass = Util.parameterizedType(propertyDescriptor);

                        if (MongoEntity.class.isAssignableFrom(targetClass)) {
                            return new MongoEntityFieldHandler_CollectionEntity(entityClass, propertyDescriptor, jsonObjectKey, targetClass, jsonEntityManager);
                        }

                        if (targetClass == String.class) {
                            return new JsonEntityFieldHandler_CollectionString(entityClass, propertyDescriptor, jsonObjectKey);
                        }

                        if (targetClass == Date.class) {
                            return new JsonEntityFieldHandler_CollectionDate(entityClass, propertyDescriptor, jsonObjectKey);
                        }

                        if (isBooleanType(targetClass)) {
                            return new JsonEntityFieldHandler_CollectionBoolean(entityClass, propertyDescriptor, jsonObjectKey);
                        }

                        if (isIntegerType(targetClass)) {
                            return new JsonEntityFieldHandler_CollectionInteger(entityClass, propertyDescriptor, jsonObjectKey);
                        }

                        if (isLongType(targetClass)) {
                            return new JsonEntityFieldHandler_CollectionLong(entityClass, propertyDescriptor, jsonObjectKey);
                        }

                        if (isFloatType(targetClass)) {
                            return new JsonEntityFieldHandler_CollectionFloat(entityClass, propertyDescriptor, jsonObjectKey);
                        }

                        if (isDoubleType(targetClass)) {
                            return new JsonEntityFieldHandler_CollectionDouble(entityClass, propertyDescriptor, jsonObjectKey);
                        }

                        if (Enum.class.isAssignableFrom(targetClass)) {
                            return new JsonEntityFieldHandler_CollectionEnum(entityClass, propertyDescriptor, jsonObjectKey, targetClass);
                        }

                        return new JsonEntityFieldHandler_CollectionEmbed(entityClass, propertyDescriptor, jsonObjectKey, targetClass);
                    }

                    throw new RuntimeException("类型无法处理：" + propertyDescriptor);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
