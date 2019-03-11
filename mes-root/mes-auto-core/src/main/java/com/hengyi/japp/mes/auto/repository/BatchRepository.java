package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.BatchQuery;
import com.hengyi.japp.mes.auto.domain.Batch;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface BatchRepository {

    Single<Batch> create();

    Single<Batch> save(Batch batch);

    Single<Batch> find(String id);

    Single<BatchQuery.Result> query(BatchQuery query);

    Flowable<Batch> list();

    Maybe<Batch> findByBatchNo(String batchNo);
}
