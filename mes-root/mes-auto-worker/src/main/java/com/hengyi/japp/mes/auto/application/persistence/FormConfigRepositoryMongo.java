package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.query.FormConfigQuery;
import com.hengyi.japp.mes.auto.domain.FormConfig;
import com.hengyi.japp.mes.auto.repository.FormConfigRepository;
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
public class FormConfigRepositoryMongo extends MongoEntityRepository<FormConfig> implements FormConfigRepository {

    @Inject
    private FormConfigRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Flowable<FormConfig> autoComplete(String q) {
        if (StringUtils.isBlank(q)) {
            return Flowable.empty();
        }
        final Pattern pattern = Pattern.compile(q, CASE_INSENSITIVE);
        final Bson qFilter = Filters.regex("name", pattern);
        final JsonObject query = unDeletedQuery(qFilter);
        final FindOptions findOptions = new FindOptions()
                .setLimit(10);
        return mongoClient.rxFindWithOptions(collectionName, query, findOptions)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy);
    }

    @Override
    public Single<FormConfigQuery.Result> query(FormConfigQuery formConfigQuery) {
        final int first = formConfigQuery.getFirst();
        final int pageSize = formConfigQuery.getPageSize();
        final FormConfigQuery.Result.ResultBuilder builder = FormConfigQuery.Result.builder().first(first).pageSize(pageSize);
        final FindOptions findOptions = new FindOptions().setSkip(first).setLimit(pageSize);

        final Bson qFilter = Optional.ofNullable(formConfigQuery.getQ())
                .filter(StringUtils::isNotBlank)
                .map(q -> {
                    final Pattern pattern = Pattern.compile(q, CASE_INSENSITIVE);
                    return Filters.regex("name", pattern);
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
                .doOnSuccess(builder::formConfigs)
                .ignoreElement();

        return Completable.mergeArray(query$, count$).toSingle(() -> builder.build());
    }
}
