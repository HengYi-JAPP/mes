package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.Corporation;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface CorporationRepository {

    Single<Corporation> create();

    Single<Corporation> save(Corporation corporation);

    Single<Corporation> find(String id);

    Flowable<Corporation> list();
}
