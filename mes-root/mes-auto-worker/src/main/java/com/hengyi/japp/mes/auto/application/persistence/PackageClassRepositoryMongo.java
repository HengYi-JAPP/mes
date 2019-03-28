package com.hengyi.japp.mes.auto.application.persistence;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil;
import com.hengyi.japp.mes.auto.domain.PackageClass;
import com.hengyi.japp.mes.auto.repository.PackageClassRepository;
import io.reactivex.Maybe;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class PackageClassRepositoryMongo extends MongoEntityRepository<PackageClass> implements PackageClassRepository {

    @Inject
    private PackageClassRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Maybe<PackageClass> findByName(String name) {
        final JsonObject query = MongoUtil.query(eq("name", name));
        return mongoClient.rxFind(collectionName, query).flatMapMaybe(list -> {
            if (J.isEmpty(list)) {
                return Maybe.empty();
            }
            return rxCreateMongoEntiy(list.get(0)).toMaybe();
        });
    }
}
