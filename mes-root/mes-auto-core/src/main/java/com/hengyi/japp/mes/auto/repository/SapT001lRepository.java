package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.SapT001l;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-11-11
 */
public interface SapT001lRepository {

    Single<SapT001l> create();

    Single<SapT001l> save(SapT001l sapT001l);

    Single<SapT001l> find(String lgort);

    Flowable<SapT001l> list();
}
