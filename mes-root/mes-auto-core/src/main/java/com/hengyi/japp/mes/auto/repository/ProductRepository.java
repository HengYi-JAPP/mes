package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.Product;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-25
 */
public interface ProductRepository {

    Single<Product> create();

    Single<Product> find(String id);

    Flowable<Product> list();

    Single<Product> save(Product product);
}
