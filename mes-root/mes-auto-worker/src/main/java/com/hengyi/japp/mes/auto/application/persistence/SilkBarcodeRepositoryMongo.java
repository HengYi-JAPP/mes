package com.hengyi.japp.mes.auto.application.persistence;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.SilkBarcodeService;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.query.SilkBarcodeQuery;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.SilkBarcode;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeRepository;
import com.hengyi.japp.mes.auto.search.lucene.SilkBarcodeLucene;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.redis.RedisClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.conversions.Bson;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Date;

import static com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil.unDeletedQuery;
import static com.mongodb.client.model.Filters.eq;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class SilkBarcodeRepositoryMongo extends MongoEntityRepository<SilkBarcode> implements SilkBarcodeRepository {
    private final SilkBarcodeLucene silkBarcodeLucene;
    private final RedisClient redisClient;
//    public static final Semaphore semaphore = new Semaphore(1);

    @Inject
    private SilkBarcodeRepositoryMongo(MongoEntiyManager mongoEntiyManager, SilkBarcodeLucene silkBarcodeLucene, RedisClient redisClient) {
        super(mongoEntiyManager);
        this.silkBarcodeLucene = silkBarcodeLucene;
        this.redisClient = redisClient;
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
        final JsonObject query = unDeletedQuery(eq("code", code));
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

    @Override
    public Single<SilkBarcode> findByAuto(Principal principal, LineMachine lineMachine, Batch batch, long timestamp) {
        final Bson lineMachineFilter = eq("lineMachine", lineMachine.getId());
        final Bson timestampFilter = eq("autoDoffingTimestamp", timestamp);
        final JsonObject query = unDeletedQuery(lineMachineFilter, timestampFilter);
        return mongoClient.rxFindOne(collectionName, query, new JsonObject())
                .flatMap(it -> rxCreateMongoEntiy(it).toMaybe())
                .switchIfEmpty(getSilkBarcodeSingle(principal, lineMachine, batch, timestamp));
    }

    private Single<SilkBarcode> getSilkBarcodeSingle(Principal principal, LineMachine lineMachine, Batch batch, final long timestamp) {
        final long l = timestamp * 1000;
        final Date date = new Date(l);
        final LocalDate codeLd = J.localDate(date);
        return create().flatMap(silkBarcode -> {
            silkBarcode.setCodeDate(J.date(codeLd));
            silkBarcode.setLineMachine(lineMachine);
            silkBarcode.setBatch(batch);
            silkBarcode.setAutoDoffingTimestamp(timestamp);
            return Jvertx.getProxy(OperatorRepository.class).find(principal).flatMap(operator -> {
                silkBarcode.log(operator);
                return redisClient.rxIncr(SilkBarcodeService.key(date));
            }).flatMap(codeDoffingNum -> {
                silkBarcode.setCodeDoffingNum(codeDoffingNum);
                silkBarcode.generateCode();
                return super.save(silkBarcode);
            });
        });
    }

}
