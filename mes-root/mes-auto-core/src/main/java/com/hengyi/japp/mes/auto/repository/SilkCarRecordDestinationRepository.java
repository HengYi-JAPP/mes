package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.SilkCarRecordDestination;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-25
 */
public interface SilkCarRecordDestinationRepository {

    Single<SilkCarRecordDestination> create();

    Single<SilkCarRecordDestination> save(SilkCarRecordDestination silkCarRecordDestination);

    Single<SilkCarRecordDestination> find(String id);

    Single<SilkCarRecordDestination> find(EntityDTO dto);

    Flowable<SilkCarRecordDestination> list();
}
