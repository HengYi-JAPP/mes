package com.hengyi.japp.mes.auto.application.persistence.proxy;

import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;
import com.hengyi.japp.mes.auto.application.persistence.MongoEntity;
import com.hengyi.japp.mes.auto.application.persistence.annotations.JsonEntityProperty;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoTable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.persistence.Cacheable;
import java.beans.PropertyDescriptor;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author jzb 2018-12-20
 */
public class MongoUtil {
    public static JsonObject unDeletedQuery(Bson... filters) {
        final Stream<Bson> stream1 = filters == null ? Stream.empty() : Stream.of(filters);
        final Stream<Bson> stream2 = Stream.of(Filters.ne("deleted", true));
        return Stream.concat(stream1, stream2)
                .filter(Objects::nonNull)
                .reduce(Filters::and)
                .map(bson -> bson.toBsonDocument(BsonDocument.class, com.mongodb.MongoClient.getDefaultCodecRegistry()))
                .map(BsonDocument::toJson)
                .map(JsonObject::new)
                .get();
    }

    public static JsonObject query(Bson... filters) {
        final Stream<Bson> stream = filters == null ? Stream.empty() : Stream.of(filters);
        return stream.filter(Objects::nonNull)
                .reduce(Filters::and)
                .map(bson -> bson.toBsonDocument(BsonDocument.class, com.mongodb.MongoClient.getDefaultCodecRegistry()))
                .map(BsonDocument::toJson)
                .map(JsonObject::new)
                .orElse(new JsonObject());
    }

    public static JsonObject ascendingQuery(String... fieldNames) {
        return Optional.of(fieldNames)
                .map(Sorts::ascending)
                .map(bson -> bson.toBsonDocument(BsonDocument.class, com.mongodb.MongoClient.getDefaultCodecRegistry()))
                .map(BsonDocument::toJson)
                .map(JsonObject::new)
                .get();
    }

    public static JsonObject descendingQuery(String... fieldNames) {
        return Optional.of(fieldNames)
                .map(Sorts::descending)
                .map(bson -> bson.toBsonDocument(BsonDocument.class, com.mongodb.MongoClient.getDefaultCodecRegistry()))
                .map(BsonDocument::toJson)
                .map(JsonObject::new)
                .get();
    }

    public static JsonObject sortQuery(Bson... sorts) {
        return Optional.of(sorts)
                .map(Sorts::orderBy)
                .map(bson -> bson.toBsonDocument(BsonDocument.class, com.mongodb.MongoClient.getDefaultCodecRegistry()))
                .map(BsonDocument::toJson)
                .map(JsonObject::new)
                .get();
    }

    public static <T extends MongoEntity> String collectionName(Class<T> entityClass) {
        final MongoTable annotation = entityClass.getAnnotation(MongoTable.class);
        if (annotation != null) {
            final String value = annotation.value();
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return "T_" + entityClass.getSimpleName();
    }

    public static <T extends MongoEntity> boolean collectionCache(Class<T> entityClass) {
        final Cacheable cacheable = entityClass.getAnnotation(Cacheable.class);
        if (cacheable != null) {
            return true;
        }
        final MongoCache mongoCache = entityClass.getAnnotation(MongoCache.class);
        if (mongoCache != null) {
            return true;
        }
        final MongoTable annotation = entityClass.getAnnotation(MongoTable.class);
        if (annotation != null) {
            return annotation.cache();
        }
        return false;
    }

    public static <T extends JsonEntity> String jsonObjectKey(Class<T> entityClass, PropertyDescriptor propertyDescriptor) {
        final String fieldName = propertyDescriptor.getName();

        final JsonEntityProperty methodAnnotation = Optional.ofNullable(propertyDescriptor.getReadMethod())
                .map(it -> it.getAnnotation(JsonEntityProperty.class))
                .orElseGet(() -> Optional.ofNullable(propertyDescriptor.getWriteMethod())
                        .map(it -> it.getAnnotation(JsonEntityProperty.class))
                        .orElse(null)
                );
        final String key1 = Optional.ofNullable(methodAnnotation)
                .map(JsonEntityProperty::value)
                .filter(StringUtils::isNotBlank)
                .orElse(null);
        if (key1 != null) {
            return key1;
        }

        if (methodAnnotation != null) {
            final String value = methodAnnotation.value();
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }

        final JsonEntityProperty fieldAnnotation = FieldUtils.getAllFieldsList(entityClass)
                .stream()
                .filter(it -> fieldName.equals(it.getName()))
                .findFirst()
                .map(it -> it.getAnnotation(JsonEntityProperty.class))
                .orElse(null);
        if (fieldAnnotation != null) {
            final String value = fieldAnnotation.value();
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return fieldName;
    }
}
