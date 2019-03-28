package com.hengyi.japp.mes.auto.interfaces.ruiguan;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.SilkCarRuntimeService;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import com.hengyi.japp.mes.auto.dto.SilkCarRecordDTO;
import io.reactivex.Completable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2019-03-11
 */
@Slf4j
@Singleton
@Path("ruiguan")
@Produces(APPLICATION_JSON)
public class RuiguanResource {
    private final RuiguanService ruiguanService;
    private final SilkCarRuntimeService silkCarRuntimeService;

    @Inject
    private RuiguanResource(RuiguanService ruiguanService, SilkCarRuntimeService silkCarRuntimeService) {
        this.ruiguanService = ruiguanService;
        this.silkCarRuntimeService = silkCarRuntimeService;
    }

    @SneakyThrows
    @Path("SilkCarRecordSilkPrint")
    @POST
    public Completable print(SilkCarRecordDTO dto) {
        return silkCarRuntimeService.find(dto)
                .map(SilkCarRuntime::getSilkCarRecord)
                .flatMapCompletable(ruiguanService::printSilk);
    }

}
