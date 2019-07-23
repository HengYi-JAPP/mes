package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface GradeRepository {

    Single<Grade> create();

    Single<Grade> save(Grade grade);

    Single<Grade> find(String id);

    Single<Grade> find(EntityDTO dto);

    Flowable<Grade> list();

    Maybe<Grade> findByName(String gradeName);
}
