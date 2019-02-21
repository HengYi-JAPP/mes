package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface WorkshopRepository {

    Single<Workshop> find(EntityDTO workshop);

    Single<Workshop> create();

    Single<Workshop> save(Workshop workshop);

    Single<Workshop> find(String id);

    Flowable<Workshop> list();
}
