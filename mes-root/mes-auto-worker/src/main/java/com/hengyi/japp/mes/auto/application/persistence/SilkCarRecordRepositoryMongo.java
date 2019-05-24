package com.hengyi.japp.mes.auto.application.persistence;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.query.SilkCarRecordByWorkshopQuery;
import com.hengyi.japp.mes.auto.application.query.SilkCarRecordQuery;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.repository.SilkCarRecordRepository;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import com.hengyi.japp.mes.auto.search.lucene.SilkCarRecordLucene;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author jzb 2018-06-25
 */
@Singleton
public class SilkCarRecordRepositoryMongo extends MongoEntityRepository<SilkCarRecord> implements SilkCarRecordRepository {
    private final SilkCarRecordLucene lucene;

    @Inject
    private SilkCarRecordRepositoryMongo(MongoEntiyManager mongoEntiyManager, SilkCarRecordLucene lucene) {
        super(mongoEntiyManager);
        this.lucene = lucene;
    }

    @Override
    public Single<SilkCarRecord> save(SilkCarRecord silkCarRecord) {
        return super.save(silkCarRecord).doOnSuccess(lucene::index);
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

    @Override
    public Flowable<String> listByWorkshop(SilkCarRecordByWorkshopQuery silkCarRecordByWorkshopQuery) {
        return Single.just(silkCarRecordByWorkshopQuery)
                .map(lucene::buildRecordByWorkshop)
                .map(it -> lucene.baseQuery(it))
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::find)
                .map(silkCarRecord -> silkCarRecord.getId());
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

}
