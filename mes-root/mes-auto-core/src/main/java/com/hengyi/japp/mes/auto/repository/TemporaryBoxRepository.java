package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.TemporaryBox;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface TemporaryBoxRepository {
    Single<TemporaryBox> create();

    Single<TemporaryBox> save(TemporaryBox temporaryBox);

    Single<TemporaryBox> find(String id);

    Single<TemporaryBox> findByCode(String code);

    Flowable<TemporaryBox> list();

    Completable rxInc(String id, int count);

    default Completable rxInc(TemporaryBox temporaryBox, int count) {
        return rxInc(temporaryBox.getId(), count);
    }
}
