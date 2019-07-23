package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface LineMachineRepository {

    Single<LineMachine> create();

    Single<LineMachine> find(String id);

    Single<LineMachine> find(EntityDTO dto);

    Single<LineMachine> save(LineMachine lineMachine);

    Flowable<LineMachine> listByLineId(String lineId);

    default Flowable<LineMachine> listBy(Line line) {
        return listByLineId(line.getId());
    }

    Flowable<LineMachine> list();

    Single<LineMachine> findBy(Line line, int lineMachineItem);
}
