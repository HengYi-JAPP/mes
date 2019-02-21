package com.hengyi.japp.mes.auto.interfaces.riamb;

import com.hengyi.japp.mes.auto.interfaces.riamb.dto.RiambFetchSilkCarRecordResultDTO;
import com.hengyi.japp.mes.auto.interfaces.riamb.dto.RiambPackageBoxEventDTO;
import com.hengyi.japp.mes.auto.interfaces.riamb.dto.RiambSilkDetachEventDTO;
import com.sun.security.auth.UserPrincipal;
import io.reactivex.Completable;
import io.reactivex.Single;

import java.security.Principal;

/**
 * 北自所接口
 *
 * @author jzb 2018-06-25
 */
public interface RiambService {
    Principal PRINCIPAL = new UserPrincipal("if_riamb");

    Single<RiambFetchSilkCarRecordResultDTO> fetchSilkCarRecord(Principal principal, String code);

    Completable silkDetach(Principal principal, RiambSilkDetachEventDTO command);

    Completable packageBox(Principal principal, RiambPackageBoxEventDTO command);
}
