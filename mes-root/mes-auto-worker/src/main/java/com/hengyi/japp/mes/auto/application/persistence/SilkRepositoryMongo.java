package com.hengyi.japp.mes.auto.application.persistence;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.query.SilkQuery;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.exception.JJsonEntityNotExsitException;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import com.hengyi.japp.mes.auto.search.lucene.SilkLucene;
import com.mongodb.client.model.Filters;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import static com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil.unDeletedQuery;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class SilkRepositoryMongo extends MongoEntityRepository<Silk> implements SilkRepository {
    private final SilkLucene silkLucene;

    @Inject
    private SilkRepositoryMongo(MongoEntiyManager mongoEntiyManager, SilkLucene silkLucene) {
        super(mongoEntiyManager);
        this.silkLucene = silkLucene;
    }

    @Override
    public Single<Silk> save(Silk silk) {
        return super.save(silk).doOnSuccess(silkLucene::index);
    }

    @Override
    public Maybe<Silk> findByCode(String code) {
        final JsonObject query = unDeletedQuery(Filters.eq("code", code));
        return mongoClient.rxFind(collectionName, query).flatMapMaybe(list -> {
            if (J.isEmpty(list)) {
                return Maybe.empty();
            }
            return rxCreateMongoEntiy(list.get(0)).toMaybe();
        });
    }

    @Override
    public Single<SilkQuery.Result> query(SilkQuery silkQuery) {
        final int first = silkQuery.getFirst();
        final int pageSize = silkQuery.getPageSize();
        final SilkQuery.Result.ResultBuilder builder = SilkQuery.Result.builder().first(first).pageSize(pageSize);

        return Single.just(silkQuery)
                .map(silkLucene::build)
                .map(it -> silkLucene.baseQuery(it, first, pageSize))
                .doOnSuccess(it -> builder.count(it.getKey()))
                .map(Pair::getValue)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::find)
                .onErrorReturn(ex -> {
                    if (ex instanceof JJsonEntityNotExsitException) {
                        final JJsonEntityNotExsitException entityNotExsitException = (JJsonEntityNotExsitException) ex;
                        silkLucene.delete(entityNotExsitException.getId());
                        return new Silk();
                    }
                    throw new RuntimeException(ex);
                })
                .toList()
                .map(silks -> builder.silks(silks).build());
    }

    @Override
    public Single<SilkQuery.Result> queryReport(SilkQuery silkQuery) {
        final int first = silkQuery.getFirst();
        final int pageSize = silkQuery.getPageSize();
        final SilkQuery.Result.ResultBuilder builder = SilkQuery.Result.builder().first(first).pageSize(pageSize);

        return Single.just(silkQuery)
                .map(silkLucene::buildReport)
                .map(it -> silkLucene.baseQuery(it, first, pageSize))
                .doOnSuccess(it -> builder.count(it.getKey()))
                .map(Pair::getValue)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::find)
                .onErrorReturn(ex -> {
                    if (ex instanceof JJsonEntityNotExsitException) {
                        final JJsonEntityNotExsitException entityNotExsitException = (JJsonEntityNotExsitException) ex;
                        silkLucene.delete(entityNotExsitException.getId());
                        return new Silk();
                    }
                    throw new RuntimeException(ex);
                })
                .toList()
                .map(silks -> builder.silks(silks).build());
    }

    @SneakyThrows
    @Override
    public void index(Silk silk) {
        silkLucene.index(silk);
    }

    @Override
    public Completable delete(Silk silk) {
        return silk._delete().doOnComplete(() -> silkLucene.delete(silk.getId()));
    }

    @Override
    public Silk find_(String id) {
        return mongoEntiyManager.find(entityClass, id);
    }

}
