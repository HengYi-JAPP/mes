package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import io.reactivex.Completable;
import io.reactivex.Single;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
public interface AdminService {

    Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.AdminManualDoffingCommand command);

    Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.AdminAutoDoffingAdaptCommand command);

    Completable unlockSilkBarcodeRepositoryMongo(Principal principal);
}
