package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.SilkQuery;
import com.hengyi.japp.mes.auto.domain.Silk;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface SilkRepository {

    Single<Silk> create();

    Single<Silk> save(Silk silk);

    Single<Silk> find(String id);

    Maybe<Silk> findByCode(String code);

    Single<SilkQuery.Result> query(SilkQuery silkQuery);

    Flowable<Silk> list();

    void index(Silk silk);

    Completable delete(Silk silk);

    Silk find_(String id);
}
