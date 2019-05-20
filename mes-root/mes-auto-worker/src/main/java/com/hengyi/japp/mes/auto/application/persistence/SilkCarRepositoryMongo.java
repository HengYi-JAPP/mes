package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil;
import com.hengyi.japp.mes.auto.application.query.SilkCarQuery;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.repository.SilkCarRepository;
import com.mongodb.client.model.Filters;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;

import java.util.Optional;
import java.util.regex.Pattern;

import static com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil.unDeletedQuery;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class SilkCarRepositoryMongo extends MongoEntityRepository<SilkCar> implements SilkCarRepository {

    @Inject
    private SilkCarRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Single<SilkCar> findByCode(String code) {
        final JsonObject query = unDeletedQuery(Filters.eq("code", code));
        return mongoClient.rxFindOne(collectionName, query, new JsonObject())
                // fixme maybe single
                .flatMapSingle(this::rxCreateMongoEntiy);
    }

    @Override
    public Flowable<SilkCar> autoComplete(String q) {
        if (StringUtils.isBlank(q)) {
            return Flowable.empty();
        }
        final Pattern pattern = Pattern.compile(q, CASE_INSENSITIVE);
        final Bson qFilter = Filters.regex("code", pattern);
        final JsonObject query = unDeletedQuery(qFilter);
        final FindOptions findOptions = new FindOptions().setLimit(10);
        return mongoClient.rxFindWithOptions(collectionName, query, findOptions)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy);
    }

    @Override
    public Single<SilkCarQuery.Result> query(SilkCarQuery silkCarQuery) {
        final int first = silkCarQuery.getFirst();
        final int pageSize = silkCarQuery.getPageSize();
        final SilkCarQuery.Result.ResultBuilder builder = SilkCarQuery.Result.builder().first(first).pageSize(pageSize);
        final FindOptions findOptions = new FindOptions().setSort(MongoUtil.ascendingQuery("code")).setSkip(first).setLimit(pageSize);
        final Bson typeFilter = Optional.ofNullable(silkCarQuery.getType())
                .filter(StringUtils::isNotBlank)
                .map(it -> Filters.eq("type", it))
                .orElse(null);
        final Bson rowFilter = Optional.ofNullable(silkCarQuery.getRow())
                .filter(row -> row > 0)
                .map(it -> Filters.eq("row", it))
                .orElse(null);
        final Bson colFilter = Optional.ofNullable(silkCarQuery.getCol())
                .filter(col -> col > 0)
                .map(it -> Filters.eq("col", it))
                .orElse(null);
        final Bson qFilter = Optional.ofNullable(silkCarQuery.getQ())
                .filter(StringUtils::isNotBlank)
                .map(q -> {
                    final Pattern pattern = Pattern.compile(q, CASE_INSENSITIVE);
                    return Filters.regex("code", pattern);
                })
                .orElse(null);
        final JsonObject query = unDeletedQuery(typeFilter, qFilter, rowFilter, colFilter);

        final Completable count$ = mongoClient.rxCount(collectionName, query)
                .doOnSuccess(builder::count)
                .ignoreElement();

        final Completable query$ = mongoClient.rxFindWithOptions(collectionName, query, findOptions)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy).toList()
                .doOnSuccess(builder::silkCars)
                .ignoreElement();

        return Completable.mergeArray(query$, count$).toSingle(() -> builder.build());
    }
}
