package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.PackageClass;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface PackageClassRepository {

    Single<PackageClass> create();

    Single<PackageClass> save(PackageClass packageClass);

    Single<PackageClass> find(String id);

    Flowable<PackageClass> list();

    Maybe<PackageClass> findByName(String name);
}
