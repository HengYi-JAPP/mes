package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.SilkCarRecordByWorkshopQuery;
import com.hengyi.japp.mes.auto.application.query.SilkCarRecordQuery;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-25
 */
public interface SilkCarRecordRepository {
    Single<SilkCarRecord> create();

    Single<SilkCarRecord> find(String id);

    Single<SilkCarRecord> save(SilkCarRecord silkCarRecord);

    Single<SilkCarRecordQuery.Result> query(SilkCarRecordQuery query);

    Flowable<String> listByWorkshop(SilkCarRecordByWorkshopQuery silkCarRecordByWorkshopQuery);

    Flowable<SilkCarRecord> list();

    void index(SilkCarRecord silkCarRecord);

    Completable delete(SilkCarRecord silkCarRecord);
}
