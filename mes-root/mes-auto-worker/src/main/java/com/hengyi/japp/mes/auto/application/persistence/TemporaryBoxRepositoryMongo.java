package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.domain.TemporaryBox;
import com.hengyi.japp.mes.auto.repository.TemporaryBoxRepository;
import com.mongodb.client.model.Filters;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import static com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil.unDeletedQuery;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class TemporaryBoxRepositoryMongo extends MongoEntityRepository<TemporaryBox> implements TemporaryBoxRepository {

    @Inject
    private TemporaryBoxRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Single<TemporaryBox> findByCode(String code) {
        final JsonObject query = unDeletedQuery(Filters.eq("code", code));
        return mongoClient.rxFindOne(collectionName, query, new JsonObject())
                // fixme maybe single
                .flatMapSingle(this::rxCreateMongoEntiy);
    }

    @Override
    public Completable rxInc(String id, int count) {
        final JsonObject query = unDeletedQuery(Filters.eq("_id", id));
        final JsonObject update = new JsonObject().put("$inc", new JsonObject().put("count", count));
        return mongoClient.rxFindOneAndUpdate(collectionName, query, update)
                .doAfterSuccess(it -> mongoEntiyManager.refresh(entityClass, id))
                .ignoreElement();
    }
}
