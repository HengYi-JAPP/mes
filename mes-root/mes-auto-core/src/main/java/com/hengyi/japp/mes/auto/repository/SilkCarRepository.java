package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.SilkCarQuery;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface SilkCarRepository {
    Single<SilkCar> create();

    Single<SilkCar> find(String id);

    Single<SilkCar> findByCode(String code);

    Single<SilkCar> save(SilkCar silkCar);

    Flowable<SilkCar> autoComplete(String q);

    Single<SilkCarQuery.Result> query(SilkCarQuery silkCarQuery);
}
