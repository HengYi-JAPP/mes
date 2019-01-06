package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface LineMachineRepository {

    Single<LineMachine> create();

    Single<LineMachine> find(String id);

    Single<LineMachine> save(LineMachine lineMachine);

    Flowable<LineMachine> listByLineId(String lineId);

    Flowable<LineMachine> listBy(Line line);

    Flowable<LineMachine> list();
}
