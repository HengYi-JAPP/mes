package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.Login;
import com.hengyi.japp.mes.auto.domain.Operator;
import io.reactivex.Maybe;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-24
 */
public interface LoginRepository {

    Single<Login> create();

    Single<Login> save(Login login);

    Single<Login> find(String id);

    Maybe<Login> findByLoginId(String loginId);

    default Single<Login> find(Operator operator) {
        return find(operator.getId());
    }

    default Single<Login> find(Principal principal) {
        return find(principal.getName());
    }

}
