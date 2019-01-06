package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.WorkshopUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Workshop;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface WorkshopService {

    Single<Workshop> create(Principal principal, WorkshopUpdateCommand command);

    Single<Workshop> update(Principal principal, String id, WorkshopUpdateCommand command);
}
