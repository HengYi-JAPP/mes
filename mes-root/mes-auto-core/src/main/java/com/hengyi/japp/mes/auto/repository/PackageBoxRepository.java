package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.PackageBoxQuery;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQueryForMeasure;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface PackageBoxRepository {
    Single<PackageBox> create();

    Single<PackageBox> save(PackageBox packageBox);

    Single<PackageBox> find(String id);

    Single<PackageBox> findByCode(String code);

    Single<PackageBoxQuery.Result> query(PackageBoxQuery packageBoxQuery);

    Single<PackageBoxQueryForMeasure.Result> query(PackageBoxQueryForMeasure packageBoxQuery);

    Single<PackageBox> findOrCreateByCode(String code);

    Flowable<PackageBox> timestampPackageBoxes(long startTimestamp, long endTimestamp);
}
