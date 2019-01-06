package com.hengyi.japp.mes.auto.application.persistence;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.query.OperatorQuery;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.mongodb.client.model.Filters;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil.unDeletedQuery;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class OperatorRepositoryMongo extends MongoEntityRepository<Operator> implements OperatorRepository {

    @Inject
    private OperatorRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Single<Operator> findByLoginId(String loginId) {
        return findByHrId(loginId).switchIfEmpty(findByOaId(loginId)).toSingle();
    }

    @Override
    public Single<Operator> findByCas(JsonObject casJsonObject) {
        final String hrId = casJsonObject.getString("uid");
        return findByLoginId(hrId);
    }

    @Override
    public Maybe<Operator> findByHrId(String hrId) {
        final JsonObject query = unDeletedQuery(Filters.eq("hrId", hrId));
        return mongoClient.rxFindOne(collectionName, query, new JsonObject())
                .filter(Objects::nonNull)
                .flatMapSingleElement(this::rxCreateMongoEntiy);
    }

    @Override
    public Maybe<Operator> findByOaId(String oaId) {
        final JsonObject query = unDeletedQuery(Filters.eq("oaId", oaId));
        return mongoClient.rxFindOne(collectionName, query, new JsonObject())
                .filter(Objects::nonNull)
                .flatMapSingleElement(this::rxCreateMongoEntiy);
    }

    @Override
    public Single<OperatorQuery.Result> query(OperatorQuery operatorQuery) {
        final int first = operatorQuery.getFirst();
        final int pageSize = operatorQuery.getPageSize();
        final OperatorQuery.Result.ResultBuilder builder = OperatorQuery.Result.builder().first(first).pageSize(pageSize);
        final FindOptions findOptions = new FindOptions().setSkip(first).setLimit(pageSize);

        final Bson qFilter = Optional.ofNullable(operatorQuery.getQ())
                .filter(StringUtils::isNotBlank)
                .map(q -> {
                    final Pattern pattern = Pattern.compile(q, CASE_INSENSITIVE);
                    final Bson hrId = Filters.regex("hrId", pattern);
                    final Bson oaId = Filters.regex("oaId", pattern);
                    final Bson name = Filters.regex("name", pattern);
                    return Filters.or(hrId, oaId, name);
                })
                .orElse(null);
        final JsonObject query = unDeletedQuery(qFilter);

        final Completable count$ = mongoClient.rxCount(collectionName, query)
                .doOnSuccess(builder::count)
                .ignoreElement();

        final Completable query$ = mongoClient.rxFindWithOptions(collectionName, query, findOptions)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy)
                .toList()
                .doOnSuccess(builder::operators)
                .ignoreElement();

        return Completable.mergeArray(query$, count$).toSingle(() -> builder.build());
    }

    @Override
    public Flowable<Operator> autoComplete(String q) {
        if (J.isBlank(q)) {
            return Flowable.empty();
        }
        final OperatorQuery operatorQuery = OperatorQuery.builder().pageSize(10).q(q).build();
        return query(operatorQuery).flattenAsFlowable(OperatorQuery.Result::getOperators);
    }

}
