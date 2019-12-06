package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.PackageBoxQueryForMeasure;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQueryOld;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface PackageBoxRepository {
    Single<PackageBox> create();

    Single<PackageBox> save(PackageBox packageBox);

    Single<PackageBox> find(String id);

    Single<PackageBox> findByCode(String code);

    Single<PackageBoxQueryOld.Result> query(PackageBoxQueryOld packageBoxQuery);

    Single<PackageBoxQueryForMeasure.Result> query(PackageBoxQueryForMeasure packageBoxQuery);

    Single<PackageBox> findOrCreateByCode(String code);
}
