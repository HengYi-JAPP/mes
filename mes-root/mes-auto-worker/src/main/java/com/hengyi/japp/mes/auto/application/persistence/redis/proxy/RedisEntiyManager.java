package com.hengyi.japp.mes.auto.application.persistence.redis.proxy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.JsonEntityManager;
import com.hengyi.japp.mes.auto.application.persistence.redis.annotations.RedisKeyPrefix;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Slf4j
@Singleton
public class RedisEntiyManager implements JsonEntityManager<RedisEntity> {
    private final RedisClient redisClient;

    @Inject
    private RedisEntiyManager(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    private <T extends RedisEntity> String key(Class<T> entityClass, String id) {
        final String prefix = Optional.ofNullable(entityClass.getAnnotation(RedisKeyPrefix.class))
                .map(RedisKeyPrefix::value)
                .filter(StringUtils::isNotBlank)
                .orElse(entityClass.getSimpleName());
        return prefix + "[" + id + "]";
    }

    @Override
    public <E extends RedisEntity> E find(Class<E> entityClass, String id) {
        throw new RuntimeException();
    }

    @Override
    public <T extends RedisEntity> Single<T> rxFind(Class<T> entityClass, String id) {
        final String key = key(entityClass, id);
        return redisClient.rxHgetall(key).flatMap(it -> createEntity(entityClass, it));
    }

    private <T extends RedisEntity> Single<T> createEntity(Class<T> entityClass, JsonObject redisJsonObject) {
        return null;
    }

    @Override
    public <T extends RedisEntity> Flowable<T> rxFindAll(Class<T> entityClass) {
        return null;
    }

}
