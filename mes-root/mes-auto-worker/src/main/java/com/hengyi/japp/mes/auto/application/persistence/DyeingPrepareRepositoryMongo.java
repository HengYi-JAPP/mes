package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil;
import com.hengyi.japp.mes.auto.application.query.DyeingPrepareQuery;
import com.hengyi.japp.mes.auto.application.query.DyeingPrepareResultQuery;
import com.hengyi.japp.mes.auto.domain.DyeingPrepare;
import com.hengyi.japp.mes.auto.repository.DyeingPrepareRepository;
import com.hengyi.japp.mes.auto.search.lucene.DyeingPrepareLucene;
import com.mongodb.client.model.Filters;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class DyeingPrepareRepositoryMongo extends MongoEntityRepository<DyeingPrepare> implements DyeingPrepareRepository {
    private final DyeingPrepareLucene dyeingPrepareLucene;

    @Inject
    private DyeingPrepareRepositoryMongo(MongoEntiyManager mongoEntiyManager, DyeingPrepareLucene dyeingPrepareLucene) {
        super(mongoEntiyManager);
        this.dyeingPrepareLucene = dyeingPrepareLucene;
    }

    @Override
    public Single<DyeingPrepare> save(DyeingPrepare dyeingPrepare) {
        return super.save(dyeingPrepare).doOnSuccess(dyeingPrepareLucene::index);
    }

    @Override
    public Flowable<DyeingPrepare> qeryBySilkCarRecordId(String id) {
        final JsonObject query = MongoUtil.unDeletedQuery(Filters.eq("silkCarRecord", id));
        return mongoClient.rxFind(collectionName, query)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy);
    }

    @Override
    public Single<DyeingPrepareQuery.Result> query(DyeingPrepareQuery dyeingPrepareQuery) {
        final int first = dyeingPrepareQuery.getFirst();
        final int pageSize = dyeingPrepareQuery.getPageSize();
        final DyeingPrepareQuery.Result.ResultBuilder builder = DyeingPrepareQuery.Result.builder().first(first).pageSize(pageSize);
        final Sort sort = new Sort(new SortedNumericSortField("createDateTime", SortField.Type.LONG, true));

        return Single.just(dyeingPrepareQuery)
                .map(dyeingPrepareLucene::build)
                .map(it -> dyeingPrepareLucene.baseQuery(it, first, pageSize, sort))
                .doOnSuccess(it -> builder.count(it.getKey()))
                .map(Pair::getValue)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::find).toList()
                .map(dyeingPrepares -> builder.dyeingPrepares(dyeingPrepares).build());
    }

    @Override
    public Single<DyeingPrepareResultQuery.Result> query(DyeingPrepareResultQuery dyeingPrepareResultQuery) {
        final int first = dyeingPrepareResultQuery.getFirst();
        final int pageSize = dyeingPrepareResultQuery.getPageSize();
        final DyeingPrepareResultQuery.Result.ResultBuilder builder = DyeingPrepareResultQuery.Result.builder().first(first).pageSize(pageSize);
        final Sort sort = new Sort(new SortedNumericSortField("createDateTime", SortField.Type.LONG, true));

        return Single.just(dyeingPrepareResultQuery)
                .map(dyeingPrepareLucene::build)
                .map(it -> dyeingPrepareLucene.baseQuery(it, first, pageSize, sort))
                .doOnSuccess(it -> builder.count(it.getKey()))
                .map(Pair::getValue)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::find).toList()
                .map(dyeingPrepares -> builder.dyeingPrepares(dyeingPrepares).build());
    }

}
