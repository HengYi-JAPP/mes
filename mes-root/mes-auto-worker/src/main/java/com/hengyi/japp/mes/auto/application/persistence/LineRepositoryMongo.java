package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.query.LineQuery;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.repository.LineRepository;
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
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class LineRepositoryMongo extends MongoEntityRepository<Line> implements LineRepository {

    @Inject
    private LineRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Single<Line> findByName(String lineName) {
        final JsonObject query = unDeletedQuery(eq("name", lineName));
        return mongoClient.rxFindOne(collectionName, query, new JsonObject())
                .flatMapSingle(this::rxCreateMongoEntiy);
    }

    @Override
    public Flowable<Line> autoComplete(String q) {
        if (StringUtils.isBlank(q)) {
            return Flowable.empty();
        }
        final Pattern pattern = Pattern.compile(q, CASE_INSENSITIVE);
        final Bson qFilter = regex("name", pattern);
        final JsonObject query = unDeletedQuery(qFilter);
        final FindOptions findOptions = new FindOptions()
                .setLimit(10);
        return mongoClient.rxFindWithOptions(collectionName, query, findOptions)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy);
    }

    @Override
    public Single<LineQuery.Result> query(LineQuery lineQuery) {
        final int first = lineQuery.getFirst();
        final int pageSize = lineQuery.getPageSize();
        final LineQuery.Result.ResultBuilder builder = LineQuery.Result.builder().first(first).pageSize(pageSize);
        final FindOptions findOptions = new FindOptions().setSkip(first).setLimit(pageSize);

        final Bson workshopFilter = Optional.ofNullable(lineQuery.getWorkshopId())
                .filter(StringUtils::isNotBlank)
                .map(workshopId -> Filters.eq("workshop", workshopId))
                .orElse(null);
        final Bson qFilter = Optional.ofNullable(lineQuery.getQ())
                .filter(StringUtils::isNotBlank)
                .map(q -> {
                    final Pattern pattern = Pattern.compile(q, CASE_INSENSITIVE);
                    return regex("name", pattern);
                })
                .orElse(null);
        final JsonObject query = unDeletedQuery(workshopFilter, qFilter);

        final Completable count$ = mongoClient.rxCount(collectionName, query)
                .doOnSuccess(builder::count)
                .ignoreElement();

        final Completable query$ = mongoClient.rxFindWithOptions(collectionName, query, findOptions)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy)
                .toList()
                .doOnSuccess(builder::lines)
                .ignoreElement();

        return Completable.mergeArray(query$, count$).toSingle(() -> builder.build());
    }

    @Override
    public Flowable<Line> listByWorkshopId(String id) {
        final JsonObject query = unDeletedQuery(Filters.eq("workshop", id));
        return mongoClient.rxFind(collectionName, query)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy);
    }

    @Override
    public Flowable<Line> listBy(Workshop workshop) {
        return listByWorkshopId(workshop.getId());
    }

}
