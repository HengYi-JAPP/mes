package com.hengyi.japp.mes.auto.repository;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.application.query.ExceptionRecordQuery;
import com.hengyi.japp.mes.auto.domain.ExceptionRecord;
import com.hengyi.japp.mes.auto.domain.Silk;
import io.reactivex.Maybe;
import io.reactivex.Single;

import java.util.Optional;

/**
 * @author jzb 2018-06-24
 */
public interface ExceptionRecordRepository {

    Single<ExceptionRecord> create();

    Single<ExceptionRecord> save(ExceptionRecord exceptionRecord);

    Single<ExceptionRecord> find(String id);

    Maybe<ExceptionRecord> findBySilkId(String silkId);

    default Maybe<ExceptionRecord> findBy(Silk silk) {
        return Optional.ofNullable(silk)
                .map(Silk::getId)
                .filter(J::nonBlank)
                .map(this::findBySilkId)
                .orElse(Maybe.empty());
    }

    Single<ExceptionRecordQuery.Result> query(ExceptionRecordQuery query);

}
