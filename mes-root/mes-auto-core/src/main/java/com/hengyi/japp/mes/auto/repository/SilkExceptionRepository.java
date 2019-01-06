package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.SilkException;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-25
 */
public interface SilkExceptionRepository {

    Single<SilkException> create();

    Single<SilkException> save(SilkException silkException);

    Single<SilkException> find(String id);

    Flowable<SilkException> list();
}
