package com.hengyi.japp.mes.auto.application.persistence.redis.proxy;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.redis.RedisClient;

import java.lang.reflect.ParameterizedType;

/**
 * @author jzb 2018-06-29
 */
public abstract class RedisEntityRepository<T extends RedisEntity> {
    protected final RedisClient redisClient;
    protected final RedisEntiyManager redisEntiyManager;
    protected final Class<T> entityClass;

    protected RedisEntityRepository(RedisClient redisClient, RedisEntiyManager redisEntiyManager) {
        this.redisClient = redisClient;
        this.redisEntiyManager = redisEntiyManager;
        entityClass = entityClass();
    }

    private Class<T> entityClass() {
        ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    public Single<T> save(T workshop) {
        return workshop._save().flatMap(this::find);
    }

    public Single<T> find(String id) {
        return redisEntiyManager.rxFind(entityClass, id);
    }

    public Flowable<T> findAll() {
        return redisEntiyManager.rxFindAll(entityClass);
    }

}
