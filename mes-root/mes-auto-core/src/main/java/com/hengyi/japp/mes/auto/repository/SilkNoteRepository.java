package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.SilkNote;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-25
 */
public interface SilkNoteRepository {

    Single<SilkNote> create();

    Single<SilkNote> save(SilkNote silkNote);

    Single<SilkNote> find(String id);

    Flowable<SilkNote> list();
}
