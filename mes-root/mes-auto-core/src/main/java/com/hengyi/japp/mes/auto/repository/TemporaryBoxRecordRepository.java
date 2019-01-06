package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.TemporaryBoxRecord;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface TemporaryBoxRecordRepository {

    Single<TemporaryBoxRecord> create();

    Single<TemporaryBoxRecord> save(TemporaryBoxRecord temporaryBoxRecord);

    Single<TemporaryBoxRecord> find(String id);

}
