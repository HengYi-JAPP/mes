package com.hengyi.japp.mes.auto.application.persistence.proxy;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.MongoEntity;
import com.hengyi.japp.mes.auto.exception.JJsonEntityNotExsitException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Collection;

@Slf4j
@Singleton
public class MongoEntiyManager implements CachedJsonEntityManager<MongoEntity> {
    @Getter
    private final MongoClient mongoClient;
    @Getter
    private final MongoDatabase mongoDatabase;
    private final LoadingCache<Pair<Class, String>, JsonObject> cache = CacheBuilder.newBuilder()
            .maximumSize(100000)
            .build(new CacheLoader<>() {
                @Override
                public JsonObject load(Pair<Class, String> pair) throws Exception {
                    final Class entityClass = pair.getLeft();
                    final String id = pair.getRight();
                    final String collectionName = MongoUtil.collectionName(entityClass);
                    final MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
                    final Bson filter = Filters.eq("_id", id);
                    final Document document = collection.find(filter).first();
                    return new JsonObject(document.toJson());
                }
            });

    @Inject
    private MongoEntiyManager(MongoClient mongoClient, MongoDatabase mongoDatabase) {
        this.mongoClient = mongoClient;
        this.mongoDatabase = mongoDatabase;
//        final CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
//        final Cache<String, String> jcache = cacheManager.getCache("dd");
//        jcache.containsKey("");
//        jcache.
    }

    @Override
    public <T extends MongoEntity> void refresh(Class<T> entityClass, String id) {
        cache.refresh(Pair.of(entityClass, id));
    }

    @SneakyThrows
    @Override
    public <T extends MongoEntity> T find(Class<T> entityClass, String id) {
        if (MongoUtil.collectionCache(entityClass)) {
            final JsonObject originJsonObject = cache.get(Pair.of(entityClass, id));
            return createMongoEntiy(entityClass, originJsonObject);
        }
        final String collectionName = MongoUtil.collectionName(entityClass);
        final MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        final Bson filter = Filters.eq("_id", id);
        final Document document = collection.find(filter).first();
        final JsonObject originJsonObject = new JsonObject(document.toJson());
        return createMongoEntiy(entityClass, originJsonObject);
    }

    @Override
    public <T extends MongoEntity> Single<T> rxFind(Class<T> entityClass, String id) {
        final String collectionName = MongoUtil.collectionName(entityClass);
        final JsonObject query = new JsonObject().put("_id", id);
        return mongoClient.rxFindOne(collectionName, query, new JsonObject())
                // fixme maybe single
                .flatMapSingle(it -> {
                    if (it == null) {
                        log.error(entityClass + "[" + id + "]");
                        throw new JJsonEntityNotExsitException(entityClass, id);
                    }
                    return rxCreateMongoEntiy(entityClass, it);
                });
    }

    @Override
    public <T extends MongoEntity> Flowable<T> rxFindAll(Class<T> entityClass) {
        final String collectionName = MongoUtil.collectionName(entityClass);
        return mongoClient.rxFind(collectionName, new JsonObject())
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(it -> rxCreateMongoEntiy(entityClass, it))
                .filter(it -> !it.isDeleted());
    }

    public <T extends MongoEntity> Single<T> rxCreateMongoEntiy(Class<T> entityClass) {
        return rxCreateMongoEntiy(entityClass, new JsonObject());
    }

    public <T extends MongoEntity> Single<T> rxCreateMongoEntiy(Class<T> entityClass, JsonObject originJsonObject) {
        return Single.fromCallable(() -> createMongoEntiy(entityClass, originJsonObject));
    }

    public <T extends MongoEntity> T createMongoEntiy(Class<T> entityClass, JsonObject originJsonObject) {
        final Collection<JsonEntityFieldHandler> fieldHandlers = JsonEntityFieldHandlerFactory.get(this, entityClass);
        final MongoEntityInterceptor interceptor = new MongoEntityInterceptor(entityClass, fieldHandlers, originJsonObject, this);

        final Enhancer e = new Enhancer();
        e.setSuperclass(entityClass);
        e.setCallback(interceptor);
        return (T) e.create();
    }

}
