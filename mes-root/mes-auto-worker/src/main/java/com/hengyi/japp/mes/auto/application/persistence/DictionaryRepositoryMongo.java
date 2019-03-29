package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil;
import com.hengyi.japp.mes.auto.domain.Dictionary;
import com.hengyi.japp.mes.auto.repository.DictionaryRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author liuyuan
 * @create 2019-03-14 15:14
 * @description
 **/
@Slf4j
@Singleton
public class DictionaryRepositoryMongo extends MongoEntityRepository<Dictionary> implements DictionaryRepository {
    @Inject
    private DictionaryRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Flowable<Dictionary> findByKey(String key) {
        final JsonObject query = MongoUtil.query(eq("key", key));
        return mongoClient.rxFind(collectionName, query)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy);
    }

    @Override
    public Completable delete(String id) {
        final JsonObject query = MongoUtil.query(eq("_id", id));
        return mongoClient.rxFindOneAndDelete(collectionName, query).ignoreElement();
    }

}
