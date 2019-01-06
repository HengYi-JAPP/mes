package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.PackageBoxFlipQuery;
import com.hengyi.japp.mes.auto.domain.PackageBoxFlip;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface PackageBoxFlipRepository {

    Single<PackageBoxFlip> create();

    Single<PackageBoxFlip> save(PackageBoxFlip packageBoxFlip);

    Single<PackageBoxFlip> find(String id);

    Single<PackageBoxFlipQuery.Result> query(PackageBoxFlipQuery query);

    Flowable<PackageBoxFlip> list();

}
