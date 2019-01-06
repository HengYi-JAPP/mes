package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil;
import com.hengyi.japp.mes.auto.application.query.BatchQuery;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.repository.BatchRepository;
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

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class BatchRepositoryMongo extends MongoEntityRepository<Batch> implements BatchRepository {

    @Inject
    private BatchRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Single<BatchQuery.Result> query(BatchQuery batchQuery) {
        final int first = batchQuery.getFirst();
        final int pageSize = batchQuery.getPageSize();
        final BatchQuery.Result.ResultBuilder builder = BatchQuery.Result.builder().first(first).pageSize(pageSize);
        final FindOptions findOptions = new FindOptions().setSkip(first).setLimit(pageSize);

        final Bson workshopFilter = Optional.ofNullable(batchQuery.getWorkshopId())
                .filter(StringUtils::isNotBlank)
                .map(it -> Filters.eq("workshop", it))
                .orElse(null);
        final Bson qFilter = Optional.ofNullable(batchQuery.getQ())
                .filter(StringUtils::isNotBlank)
                .map(q -> {
                    final Pattern pattern = Pattern.compile(q, CASE_INSENSITIVE);
                    return Filters.regex("batchNo", pattern);
                })
                .orElse(null);
        final JsonObject query = MongoUtil.unDeletedQuery(workshopFilter, qFilter);

        final Completable count$ = mongoClient.rxCount(collectionName, query)
                .doOnSuccess(builder::count)
                .ignoreElement();

        final Completable query$ = mongoClient.rxFindWithOptions(collectionName, query, findOptions)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy)
                .toList()
                .doOnSuccess(builder::batches)
                .ignoreElement();

        return Completable.mergeArray(query$, count$).toSingle(() -> builder.build());
    }
}
