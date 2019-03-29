package com.hengyi.japp.mes.auto.application.persistence;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil;
import com.hengyi.japp.mes.auto.application.query.DictionaryQuery;
import com.hengyi.japp.mes.auto.domain.Dictionary;
import com.hengyi.japp.mes.auto.repository.DictionaryRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;

import java.util.List;

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
    public Maybe<Dictionary> findByKey(String key) {
        final JsonObject query = MongoUtil.query(eq("key", key));
        return mongoClient.rxFind(collectionName, query)
                .flatMapMaybe(list -> {
                    if (J.isEmpty(list)) {
                        return Maybe.empty();
                    }
                    return rxCreateMongoEntiy(list.get(0)).toMaybe();
                });
    }

    @Override
    public Single<DictionaryQuery.Result> query(DictionaryQuery dictionaryQuery) {
        final int first = dictionaryQuery.getFirst();
        final int pageSize = dictionaryQuery.getPageSize();
        final DictionaryQuery.Result.ResultBuilder builder = DictionaryQuery.Result.builder().first(first).pageSize(pageSize);
        final FindOptions findOptions = new FindOptions().setSkip(first).setLimit(pageSize);
        final Completable count$ = mongoClient.rxCount(collectionName, null)
                .doOnSuccess(builder::count)
                .ignoreElement();

        final Completable query$ = mongoClient.rxFindWithOptions(collectionName, null, findOptions)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy)
                .toList()
                .doOnSuccess(builder::dictionaries)
                .ignoreElement();

        return Completable.mergeArray(query$, count$).toSingle(() -> builder.build());
    }
}
