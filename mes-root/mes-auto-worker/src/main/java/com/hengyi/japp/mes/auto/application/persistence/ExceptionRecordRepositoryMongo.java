package com.hengyi.japp.mes.auto.application.persistence;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.ApplicationEvents;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil;
import com.hengyi.japp.mes.auto.application.query.ExceptionRecordQuery;
import com.hengyi.japp.mes.auto.domain.ExceptionRecord;
import com.hengyi.japp.mes.auto.repository.ExceptionRecordRepository;
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

import static com.mongodb.client.model.Filters.eq;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class ExceptionRecordRepositoryMongo extends MongoEntityRepository<ExceptionRecord> implements ExceptionRecordRepository {
    private final ApplicationEvents applicationEvents;

    @Inject
    private ExceptionRecordRepositoryMongo(MongoEntiyManager mongoEntiyManager, ApplicationEvents applicationEvents) {
        super(mongoEntiyManager);
        this.applicationEvents = applicationEvents;
    }

    @Override
    public Single<ExceptionRecord> save(ExceptionRecord exceptionRecord) {
        return super.save(exceptionRecord).doOnSuccess(applicationEvents::fire);
    }

    @Override
    public Maybe<ExceptionRecord> findBySilkId(String silkId) {
        final JsonObject query = MongoUtil.query(eq("silk", silkId));
        return mongoClient.rxFind(collectionName, query).flatMapMaybe(list -> {
            if (J.isEmpty(list)) {
                return Maybe.empty();
            }
            return rxCreateMongoEntiy(list.get(0)).toMaybe();
        });
    }

    @Override
    public Single<ExceptionRecordQuery.Result> query(ExceptionRecordQuery exceptionRecordQuery) {
        final int first = exceptionRecordQuery.getFirst();
        final int pageSize = exceptionRecordQuery.getPageSize();
        final ExceptionRecordQuery.Result.ResultBuilder builder = ExceptionRecordQuery.Result.builder().first(first).pageSize(pageSize);
        final FindOptions findOptions = new FindOptions().setSkip(first).setLimit(pageSize);

        final Bson lineMachineFilter = J.emptyIfNull(exceptionRecordQuery.getLineMachineIds())
                .parallelStream()
                .filter(StringUtils::isNotBlank)
                .map(it -> eq("lineMachine", it))
                .reduce(Filters::or)
                .orElse(null);
        final Bson handledFilter = eq("handled", exceptionRecordQuery.isHandled());
        final JsonObject query = MongoUtil.unDeletedQuery(lineMachineFilter, handledFilter);
        if (!exceptionRecordQuery.isHandled()) {
            return mongoClient.rxFind(collectionName, query)
                    .flatMapPublisher(Flowable::fromIterable)
                    .flatMapSingle(this::rxCreateMongoEntiy)
                    .toList()
                    .map(builder::result)
                    .map(ExceptionRecordQuery.Result.ResultBuilder::build);
        }
        final Completable count$ = mongoClient.rxCount(collectionName, query)
                .doOnSuccess(builder::count)
                .ignoreElement();
        final Completable query$ = mongoClient.rxFindWithOptions(collectionName, query, findOptions)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy)
                .toList()
                .doOnSuccess(builder::result)
                .ignoreElement();
        return Completable.mergeArray(query$, count$).toSingle(() -> builder.build());
    }
}
