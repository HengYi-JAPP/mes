package com.hengyi.japp.mes.auto.interfaces.ruiguan;

import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.sun.security.auth.UserPrincipal;
import io.reactivex.Completable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;

/**
 * @author jzb 2019-03-11
 */
public interface RuiguanService {
    Logger LOG = LoggerFactory.getLogger(RuiguanService.class);
    Principal PRINCIPAL = new UserPrincipal("if_ruiguan");

    Completable handle(Principal principal, SilkCarRuntimeInitEvent.RuiguanAutoDoffingCommand command);

    Completable printSilk(SilkCarRecord silkCarRecord);
}
