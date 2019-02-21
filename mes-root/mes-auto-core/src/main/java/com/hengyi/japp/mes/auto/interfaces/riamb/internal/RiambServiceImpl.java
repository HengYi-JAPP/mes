package com.hengyi.japp.mes.auto.interfaces.riamb.internal;

import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.interfaces.riamb.RiambService;
import com.hengyi.japp.mes.auto.interfaces.riamb.dto.RiambFetchSilkCarRecordResultDTO;
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
}
