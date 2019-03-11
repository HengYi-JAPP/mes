package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-22
 */
public interface SilkCarRecordService {

    Single<SilkCarRecord> save(SilkCarRuntime silkCarRuntime);

    void handle(SilkCarRuntimeInitEvent.AutoDoffingSilkCarRuntimeCreateCommand command);
}
