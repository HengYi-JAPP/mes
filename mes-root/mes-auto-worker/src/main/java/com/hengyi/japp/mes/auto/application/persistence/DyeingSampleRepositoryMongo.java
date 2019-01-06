package com.hengyi.japp.mes.auto.application.persistence;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil;
import com.hengyi.japp.mes.auto.domain.DyeingSample;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.repository.DyeingSampleRepository;
import com.mongodb.client.model.Filters;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class DyeingSampleRepositoryMongo extends MongoEntityRepository<DyeingSample> implements DyeingSampleRepository {

    @Inject
    private DyeingSampleRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Single<DyeingSample> save(DyeingSample dyeingSample) {
        final String id = Optional.ofNullable(dyeingSample.getSilk())
                .map(Silk::getId)
                .orElse(dyeingSample.getCode());
        dyeingSample.setId(id);
        return super.save(dyeingSample);
    }

    @Override
    public Maybe<DyeingSample> findByCode(String code) {
        final JsonObject query = MongoUtil.unDeletedQuery(Filters.eq("code", code));
        return mongoClient.rxFind(collectionName, query).flatMapMaybe(list -> {
            if (J.isEmpty(list)) {
                return Maybe.empty();
            }
            return rxCreateMongoEntiy(list.get(0)).toMaybe();
        });
    }
}
