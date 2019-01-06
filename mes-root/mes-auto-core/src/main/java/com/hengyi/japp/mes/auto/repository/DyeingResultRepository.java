package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.DyeingResult;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface DyeingResultRepository {

    Single<DyeingResult> create();

    Single<DyeingResult> save(DyeingResult dyeingResult);

    Single<DyeingResult> find(String id);

    Flowable<DyeingResult> list();

}
