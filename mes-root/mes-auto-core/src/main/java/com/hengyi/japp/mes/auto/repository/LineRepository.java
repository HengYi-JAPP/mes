package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.LineQuery;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface LineRepository {

    Single<Line> create();

    Single<Line> find(String id);

    Single<Line> find(EntityDTO dto);

    Single<Line> save(Line line);

    Flowable<Line> autoComplete(String q);

    Single<LineQuery.Result> query(LineQuery lineQuery);

    Flowable<Line> listByWorkshopId(String id);

    default Flowable<Line> listBy(Workshop workshop) {
        return listByWorkshopId(workshop.getId());
    }

    Flowable<Line> list();

    Single<Line> findByName(String lineName);
}
