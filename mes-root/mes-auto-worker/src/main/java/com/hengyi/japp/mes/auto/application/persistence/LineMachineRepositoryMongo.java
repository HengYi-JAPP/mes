package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.repository.LineMachineRepository;
import com.mongodb.client.model.Filters;
import io.reactivex.Flowable;
import io.vertx.core.json.JsonObject;

/**
 * @author jzb 2018-06-25
 */
@Singleton
public class LineMachineRepositoryMongo extends MongoEntityRepository<LineMachine> implements LineMachineRepository {

    @Inject
    private LineMachineRepositoryMongo(MongoEntiyManager mongoEntiyFactory) {
        super(mongoEntiyFactory);
    }

    @Override
    public Flowable<LineMachine> listByLineId(String lineId) {
        final JsonObject query = MongoUtil.unDeletedQuery(Filters.eq("line", lineId));
        return mongoClient.rxFind(collectionName, query)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy);
    }

    @Override
    public Flowable<LineMachine> listBy(Line line) {
        return listByLineId(line.getId());
    }

}
