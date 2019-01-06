package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.DyeingPrepareQuery;
import com.hengyi.japp.mes.auto.application.query.DyeingPrepareResultQuery;
import com.hengyi.japp.mes.auto.domain.DyeingPrepare;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface DyeingPrepareRepository {

    Single<DyeingPrepare> create();

    Single<DyeingPrepare> save(DyeingPrepare dyeingPrepare);

    Single<DyeingPrepare> find(String id);

    Flowable<DyeingPrepare> qeryBySilkCarRecordId(String id);

    Single<DyeingPrepareQuery.Result> query(DyeingPrepareQuery dyeingPrepareQuery);

    Single<DyeingPrepareResultQuery.Result> query(DyeingPrepareResultQuery dyeingPrepareResultQuery);
}
