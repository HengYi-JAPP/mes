package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.domain.Login;
import com.hengyi.japp.mes.auto.repository.LoginRepository;
import com.mongodb.client.model.Filters;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil.unDeletedQuery;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class LoginRepositoryMongo extends MongoEntityRepository<Login> implements LoginRepository {

    @Inject
    private LoginRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Single<Login> save(Login login) {
        login.setId(login.getOperator().getId());
        return super.save(login);
    }

    @Override
    public Maybe<Login> findByLoginId(String loginId) {
        final JsonObject query = unDeletedQuery(Filters.eq("loginId", loginId));
        return mongoClient.rxFindOne(collectionName, query, new JsonObject())
                .filter(Objects::nonNull)
                .filter(it -> !it.isEmpty())
                .flatMapSingleElement(this::rxCreateMongoEntiy);
    }
}
