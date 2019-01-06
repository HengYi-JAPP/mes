package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.OperatorGroup;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface OperatorGroupRepository {

    Single<OperatorGroup> create();

    Single<OperatorGroup> find(String id);

    Single<OperatorGroup> save(OperatorGroup operatorGroup);

    Flowable<OperatorGroup> list();
}
