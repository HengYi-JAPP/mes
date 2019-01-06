package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.Product;
import com.hengyi.japp.mes.auto.domain.ProductProcess;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-25
 */
public interface ProductProcessRepository {

    Single<ProductProcess> create();

    Single<ProductProcess> save(ProductProcess productProcess);

    Single<ProductProcess> find(String id);

    Flowable<ProductProcess> listByProductId(String productId);

    Flowable<ProductProcess> listBy(Product product);
}
