package com.hengyi.japp.mes.auto.application.persistence.proxy;

import com.hengyi.japp.mes.auto.application.persistence.MongoEntity;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

import java.lang.reflect.ParameterizedType;

/**
 * @author jzb 2018-06-29
 */
public abstract class MongoEntityRepository<T extends MongoEntity> {
    protected final MongoClient mongoClient;
    protected final MongoEntiyManager mongoEntiyManager;
    protected final Class<T> entityClass;
    protected final String collectionName;

    protected MongoEntityRepository(MongoEntiyManager mongoEntiyManager) {
        this.mongoEntiyManager = mongoEntiyManager;
        this.mongoClient = mongoEntiyManager.getMongoClient();
        entityClass = entityClass();
        collectionName = MongoUtil.collectionName(entityClass);
    }

    private Class<T> entityClass() {
        ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    protected Single<T> rxCreateMongoEntiy() {
        return mongoEntiyManager.rxCreateMongoEntiy(entityClass);
    }

    protected Single<T> rxCreateMongoEntiy(JsonObject originJsonObject) {
        return mongoEntiyManager.rxCreateMongoEntiy(entityClass, originJsonObject);
    }

    public Single<T> create() {
        return rxCreateMongoEntiy();
    }

    public Single<T> save(T t) {
        return t._save().flatMap(this::find);
    }

    public Single<T> find(EntityDTO dto) {
        return find(dto.getId());
    }

    public Single<T> find(String id) {
        return mongoEntiyManager.rxFind(entityClass, id);
    }

    public Flowable<T> list() {
        return mongoEntiyManager.rxFindAll(entityClass);
    }

}
