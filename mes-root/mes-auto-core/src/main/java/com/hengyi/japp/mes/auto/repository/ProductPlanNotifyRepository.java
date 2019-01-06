package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.ProductPlanNotifyQuery;
import com.hengyi.japp.mes.auto.domain.ProductPlanNotify;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-25
 */
public interface ProductPlanNotifyRepository {

    Single<ProductPlanNotify> create();

    Single<ProductPlanNotify> find(String id);

    Single<ProductPlanNotify> save(ProductPlanNotify productPlanNotify);

    Single<ProductPlanNotifyQuery.Result> query(ProductPlanNotifyQuery productPlanNotifyQuery);
}
