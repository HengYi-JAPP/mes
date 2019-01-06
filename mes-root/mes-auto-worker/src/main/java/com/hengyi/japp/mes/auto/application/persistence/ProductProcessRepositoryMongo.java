package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoUtil;
import com.hengyi.japp.mes.auto.domain.Product;
import com.hengyi.japp.mes.auto.domain.ProductProcess;
import com.hengyi.japp.mes.auto.repository.ProductProcessRepository;
import com.mongodb.client.model.Filters;
import io.reactivex.Flowable;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class ProductProcessRepositoryMongo extends MongoEntityRepository<ProductProcess> implements ProductProcessRepository {

    @Inject
    private ProductProcessRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Flowable<ProductProcess> listByProductId(String productId) {
        final JsonObject query = MongoUtil.unDeletedQuery(Filters.eq("product", productId));
        return mongoClient.rxFind(collectionName, query)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMapSingle(this::rxCreateMongoEntiy);
    }

    @Override
    public Flowable<ProductProcess> listBy(Product product) {
        return listByProductId(product.getId());
    }
}
