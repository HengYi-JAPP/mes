package com.hengyi.japp.mes.auto.application.persistence;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.query.SilkCarRecordQuery;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.interfaces.search.SearchService;
import com.hengyi.japp.mes.auto.repository.SilkCarRecordRepository;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import com.hengyi.japp.mes.auto.search.lucene.SilkCarRecordLucene;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author jzb 2018-06-25
 */
@Singleton
public class SilkCarRecordRepositoryMongo extends MongoEntityRepository<SilkCarRecord> implements SilkCarRecordRepository {
    private final SilkCarRecordLucene lucene;
    private final SearchService searchService;

    @Inject
    private SilkCarRecordRepositoryMongo(MongoEntiyManager mongoEntiyManager, SilkCarRecordLucene lucene, SearchService searchService) {
        super(mongoEntiyManager);
        this.lucene = lucene;
        this.searchService = searchService;
    }

    @Override
    public Single<SilkCarRecord> save(SilkCarRecord silkCarRecord) {
        return super.save(silkCarRecord).doOnSuccess(it -> {
            lucene.index(it);
            searchService.index(it);
        });
    }

    @Override
    public Single<SilkCarRecordQuery.Result> query(SilkCarRecordQuery silkCarRecordQuery) {
        final int first = silkCarRecordQuery.getFirst();
        final int pageSize = silkCarRecordQuery.getPageSize();
        final SilkCarRecordQuery.Result.ResultBuilder builder = SilkCarRecordQuery.Result.builder().first(first).pageSize(pageSize);

        return Single.just(silkCarRecordQuery)
                .map(lucene::build)
                .map(it -> lucene.baseQuery(it, first, pageSize))
                .doOnSuccess(it -> builder.count(it.getKey()))
                .map(Pair::getValue)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::find).toList()
                .map(silkBarcodes -> builder.silkCarRecords(silkBarcodes).build());
    }

    @SneakyThrows
    @Override
    public void index(SilkCarRecord silkCarRecord) {
        lucene.index(silkCarRecord);
    }

    @Override
    public Completable delete(SilkCarRecord silkCarRecord) {
        if (silkCarRecord.getCarpoolDateTime() != null) {
            throw new RuntimeException("拼车,无法删除");
        }
        final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);

        final Completable delSilkCarRecord$ = silkCarRecord._delete().doOnComplete(() -> lucene.delete(silkCarRecord.getId()));
        final Completable delSilks$ = Flowable.fromIterable(silkCarRecord.initSilks())
                .map(SilkRuntime::getSilk)
                .flatMapCompletable(silkRepository::delete);
        return Completable.mergeArray(delSilkCarRecord$, delSilks$);
    }

    @Override
    public Maybe<SilkCarRecord> findByAutoId(String id) {
        final JsonObject query = new JsonObject().put("_id", id);
        return mongoClient.rxFindOne(collectionName, query, new JsonObject())
                .toSingle(new JsonObject())
                .flatMapMaybe(it -> {
                    if (it.isEmpty()) {
                        return Maybe.empty();
                    }
                    return rxCreateMongoEntiy(it).toMaybe();
                });
    }

}
