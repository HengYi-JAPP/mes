package com.hengyi.japp.mes.auto.application.persistence;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil;
import com.hengyi.japp.mes.auto.application.query.SilkBarcodeQuery;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.SilkBarcode;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeRepository;
import com.hengyi.japp.mes.auto.search.lucene.SilkBarcodeLucene;
import com.mongodb.client.model.Filters;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class SilkBarcodeRepositoryMongo extends MongoEntityRepository<SilkBarcode> implements SilkBarcodeRepository {
    private final SilkBarcodeLucene silkBarcodeLucene;
//    public static final Semaphore semaphore = new Semaphore(1);

    @Inject
    private SilkBarcodeRepositoryMongo(MongoEntiyManager mongoEntiyManager, SilkBarcodeLucene silkBarcodeLucene) {
        super(mongoEntiyManager);
        this.silkBarcodeLucene = silkBarcodeLucene;
    }

    @SneakyThrows
    @Override
    public Single<SilkBarcode> save(SilkBarcode silkBarcode) {
        if (J.isBlank(silkBarcode.getCode())) {
            silkBarcode.setCode(silkBarcode.generateCode());
        }

//        semaphore.acquire();
        final LineMachine lineMachine = silkBarcode.getLineMachine();
        final String doffingNum = silkBarcode.getDoffingNum();
        final Batch batch = silkBarcode.getBatch();
        final LocalDate codeLd = J.localDate(silkBarcode.getCodeDate());
        final Single<SilkBarcode> create$ = super.save(silkBarcode)
                .doOnSuccess(silkBarcodeLucene::index);
        return find(codeLd, lineMachine, doffingNum, batch).switchIfEmpty(create$);
//                .doAfterTerminate(semaphore::release);
    }

    @Override
    public Single<SilkBarcode> findByCode(String code) {
        final JsonObject query = MongoUtil.unDeletedQuery(Filters.eq("code", code));
        return mongoClient.rxFind(collectionName, query)
                .flatMapPublisher(Flowable::fromIterable)
                .singleOrError()
                .flatMap(this::rxCreateMongoEntiy);
    }

    @Override
    synchronized public Maybe<SilkBarcode> find(LocalDate codeLd, LineMachine lineMachine, String doffingNum, Batch batch) {
        final SilkBarcodeQuery silkBarcodeQuery = SilkBarcodeQuery.builder()
                .startLd(codeLd)
                .endLd(codeLd)
                .lineMachineId(lineMachine.getId())
                .doffingNum(doffingNum)
                .batchId(batch.getId())
                .build();
        return Single.just(silkBarcodeQuery)
                .map(silkBarcodeLucene::query)
                .flatMapMaybe(list -> {
                    if (J.isEmpty(list)) {
                        return Maybe.empty();
                    }
                    if (list.size() > 1) {
                        final String s = String.join(" ", codeLd.toString(), lineMachine.getLine().getName(), "" + lineMachine.getItem(), doffingNum, batch.getBatchNo());
                        throw new RuntimeException(s);
                    }
                    return find(IterableUtils.get(list, 0)).toMaybe();
                });
    }

    @Override
    public Single<SilkBarcode> find(LocalDate codeLd, long codeDoffingNum) {
        final SilkBarcodeQuery silkBarcodeQuery = SilkBarcodeQuery.builder()
                .startLd(codeLd)
                .endLd(codeLd)
                .codeDoffingNum(codeDoffingNum)
                .build();
        return Single.just(silkBarcodeQuery)
                .flattenAsFlowable(silkBarcodeLucene::query)
                .singleOrError()
                .flatMap(this::find);
    }

    @Override
    public Single<SilkBarcodeQuery.Result> query(SilkBarcodeQuery silkBarcodeQuery) {
        final int first = silkBarcodeQuery.getFirst();
        final int pageSize = silkBarcodeQuery.getPageSize();
        final SilkBarcodeQuery.Result.ResultBuilder builder = SilkBarcodeQuery.Result.builder().first(first).pageSize(pageSize);

        return Single.just(silkBarcodeQuery)
                .map(silkBarcodeLucene::build)
                .map(it -> silkBarcodeLucene.baseQuery(it, first, pageSize))
                .doOnSuccess(it -> builder.count(it.getKey()))
                .map(Pair::getValue)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::find).toList()
                .map(silkBarcodes -> builder.silkBarcodes(silkBarcodes).build());
    }

    @SneakyThrows
    @Override
    public void index(SilkBarcode silkBarcode) {
        silkBarcodeLucene.index(silkBarcode);
    }

    @Override
    public Completable delete(String id) {
        return find(id).flatMap(silkBarcode -> {
            silkBarcode.setDeleted(true);
            return super.save(silkBarcode);
        }).doOnSuccess(silkBarcodeLucene::index).ignoreElement();
    }
}
