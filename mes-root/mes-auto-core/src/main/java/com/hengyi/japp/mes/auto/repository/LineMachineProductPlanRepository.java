package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.LineMachineProductPlan;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface LineMachineProductPlanRepository {

    Single<LineMachineProductPlan> create();

    Single<LineMachineProductPlan> find(String id);

    Single<LineMachineProductPlan> save(LineMachineProductPlan productPlan);
}
