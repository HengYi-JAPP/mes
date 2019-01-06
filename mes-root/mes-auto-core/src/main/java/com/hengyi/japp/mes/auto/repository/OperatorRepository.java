package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.OperatorQuery;
import com.hengyi.japp.mes.auto.domain.Operator;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import java.security.Principal;

/**
 * @author jzb 2018-06-24
 */
public interface OperatorRepository {

    Single<Operator> create();

    Single<Operator> findByLoginId(String loginId);

    Single<Operator> findByCas(JsonObject jsonObject);

    default Single<Operator> find(Principal principal) {
        return find(principal.getName());
    }

    Maybe<Operator> findByHrId(String hrId);

    Maybe<Operator> findByOaId(String oaId);

    Single<Operator> find(String id);

    Single<Operator> save(Operator operator);

    Single<OperatorQuery.Result> query(OperatorQuery query);

    Flowable<Operator> autoComplete(String q);
}
