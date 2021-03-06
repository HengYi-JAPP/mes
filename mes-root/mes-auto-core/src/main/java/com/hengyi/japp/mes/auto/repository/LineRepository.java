package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.LineQuery;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.Workshop;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface LineRepository {

    Single<Line> create();

    Single<Line> find(String id);

    Single<Line> save(Line line);

    Flowable<Line> autoComplete(String q);

    Single<LineQuery.Result> query(LineQuery lineQuery);

    Flowable<Line> listByWorkshopId(String id);

    Flowable<Line> listBy(Workshop workshop);

    Flowable<Line> list();
}
