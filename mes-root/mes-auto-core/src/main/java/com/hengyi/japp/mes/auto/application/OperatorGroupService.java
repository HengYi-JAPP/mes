package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.OperatorGroupUpdateCommand;
import com.hengyi.japp.mes.auto.domain.OperatorGroup;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface OperatorGroupService {

    Single<OperatorGroup> create(Principal principal, OperatorGroupUpdateCommand command);

    Single<OperatorGroup> update(Principal principal, String id, OperatorGroupUpdateCommand command);
}
