package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.event.ToDtyConfirmEvent;
import com.hengyi.japp.mes.auto.application.event.ToDtyEvent;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import io.reactivex.Completable;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface SilkCarRecordService {

    Single<SilkCarRecord> save(SilkCarRuntime silkCarRuntime);

    Completable handle(Principal principal, ToDtyEvent.Command command);

    Completable handle(Principal principal, ToDtyConfirmEvent.Command command);
}
