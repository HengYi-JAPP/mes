package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.DyeingSample;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.exception.JJsonEntityNotExsitException;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface DyeingSampleRepository {

    Single<DyeingSample> create();

    Single<DyeingSample> save(DyeingSample dyeingSampleSilk);

    Single<DyeingSample> find(String id);

    Maybe<DyeingSample> findByCode(String code);

    default Single<DyeingSample> findOrCreateBy(Silk silk) {
        return find(silk.getId()).onErrorResumeNext(ex -> {
            if (ex instanceof JJsonEntityNotExsitException) {
                return create().map(dyeingSample -> {
                    dyeingSample.setSilk(silk);
                    return dyeingSample;
                });
            }
            return Single.error(ex);
        });
    }

    default Single<DyeingSample> findOrCreateBy(SilkRuntime silkRuntime) {
        return findOrCreateBy(silkRuntime.getSilk());
    }

}
