package com.hengyi.japp.mes.auto.interfaces.riamb.internal;

import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.interfaces.riamb.RiambService;
import com.hengyi.japp.mes.auto.interfaces.riamb.dto.RiambFetchSilkCarRecordResultDTO;
import com.hengyi.japp.mes.auto.interfaces.riamb.dto.RiambPackageBoxEventDTO;
import com.hengyi.japp.mes.auto.interfaces.riamb.dto.RiambSilkDetachEventDTO;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class RiambServiceImpl implements RiambService {

    @Override
    public Single<RiambFetchSilkCarRecordResultDTO> fetchSilkCarRecord(Principal principal, String code) {
        return null;
    }

    @Override
    public Completable silkDetach(Principal principal, RiambSilkDetachEventDTO command) {
        return null;
    }

    @Override
    public Completable packageBox(Principal principal, RiambPackageBoxEventDTO command) {
        return null;
    }
}
