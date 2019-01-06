package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.LineMachineUpdateCommand;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.LineMachineProductPlan;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface LineMachineService {

    Single<LineMachine> create(Principal principal, LineMachineUpdateCommand command);

    Single<LineMachine> update(Principal principal, String id, LineMachineUpdateCommand command);

    Flowable<LineMachineProductPlan> listTimeline(String id, String currentId, int size);
}
