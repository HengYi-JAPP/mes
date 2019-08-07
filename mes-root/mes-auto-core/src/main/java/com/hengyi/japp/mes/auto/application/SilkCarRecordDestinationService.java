package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.command.SilkCarRecordDestinationUpdateCommand;
import com.hengyi.japp.mes.auto.domain.SilkCarRecordDestination;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-21
 */
public interface SilkCarRecordDestinationService {

    Single<SilkCarRecordDestination> create(Principal principal, SilkCarRecordDestinationUpdateCommand command);

    Single<SilkCarRecordDestination> update(Principal principal, String id, SilkCarRecordDestinationUpdateCommand command);
}
