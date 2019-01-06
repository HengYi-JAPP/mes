package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil;
import com.hengyi.japp.mes.auto.application.query.ProductPlanNotifyQuery;
import com.hengyi.japp.mes.auto.domain.ProductPlanNotify;
import com.hengyi.japp.mes.auto.repository.ProductPlanNotifyRepository;
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
public class ProductPlanNotifyRepositoryMongo extends MongoEntityRepository<ProductPlanNotify> implements ProductPlanNotifyRepository {

    @Inject
    private ProductPlanNotifyRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Single<ProductPlanNotifyQuery.Result> query(ProductPlanNotifyQuery productPlanNotifyQuery) {
        final int first = productPlanNotifyQuery.getFirst();
        final int pageSize = productPlanNotifyQuery.getPageSize();
        final ProductPlanNotifyQuery.Result.ResultBuilder builder = ProductPlanNotifyQuery.Result.builder().first(first).pageSize(pageSize);
        final FindOptions findOptions = new FindOptions().setSort(MongoUtil.descendingQuery("startDate")).setSkip(first).setLimit(pageSize);

        final Bson qFilter = Optional.ofNullable(productPlanNotifyQuery.getQ())
                .filter(StringUtils::isNotBlank)
                .map(q -> {
                    final Pattern pattern = Pattern.compile(q, CASE_INSENSITIVE);
                    return Filters.regex("name", pattern);
                })
                .orElse(null);
        final JsonObject query = MongoUtil.unDeletedQuery(qFilter);

        final Completable counting$ = mongoClient.rxCount(collectionName, query)
                .doOnSuccess(builder::count)
                .ignoreElement();

        final Completable quering$ = mongoClient.rxFindWithOptions(collectionName, query, findOptions)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy).toList()
                .doOnSuccess(builder::productPlanNotifies)
                .ignoreElement();

        return Completable.mergeArray(quering$, counting$).toSingle(() -> builder.build());
    }
}
