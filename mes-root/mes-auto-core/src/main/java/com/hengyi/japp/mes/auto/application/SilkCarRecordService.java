package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.application.event.ToDtyEvent;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import io.reactivex.Completable;
import io.reactivex.Single;

import java.security.Principal;
import java.util.List;

/**
 * @author jzb 2018-06-22
 */
public interface SilkCarRecordService {

    Single<SilkCarRecord> save(SilkCarRuntime silkCarRuntime);

    Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeInitEvent.ManualDoffingCheckSilksCommand command);

    Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.ManualDoffingCommand command);

    Completable handle(Principal principal, ToDtyEvent.Command command);
}
