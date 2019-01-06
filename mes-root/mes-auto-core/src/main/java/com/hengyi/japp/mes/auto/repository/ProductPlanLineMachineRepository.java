package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.LineMachineProductPlan;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-25
 */
public interface ProductPlanLineMachineRepository {
    Single<LineMachineProductPlan> find(String id);
}
