package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.domain.Permission;
import com.hengyi.japp.mes.auto.repository.PermissionRepository;
import com.mongodb.client.model.Filters;
import io.reactivex.Flowable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;

import java.util.regex.Pattern;

import static com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil.unDeletedQuery;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class PermissionRepositoryMongo extends MongoEntityRepository<Permission> implements PermissionRepository {

    @Inject
    private PermissionRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Flowable<Permission> autoComplete(String q) {
        if (StringUtils.isBlank(q)) {
            return Flowable.empty();
        }
        final Pattern pattern = Pattern.compile(q, CASE_INSENSITIVE);
        final Bson qFilter = Filters.regex("name", pattern);
        final Bson codeFilter = Filters.regex("code", pattern);
        final JsonObject query = unDeletedQuery(Filters.or(qFilter, codeFilter));
        final FindOptions findOptions = new FindOptions()
                .setLimit(10);
        return mongoClient.rxFindWithOptions(collectionName, query, findOptions)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy);
    }
}
