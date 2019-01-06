package com.hengyi.japp.mes.auto.interfaces.facevisa;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.interfaces.facevisa.dto.AutoVisualInspectionSilkInfoDTO;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Slf4j
@Singleton
@Path("facevisa")
@Produces(APPLICATION_JSON)
public class FacevisaResource {
    private final FacevisaService facevisaService;

    @Inject
    private FacevisaResource(FacevisaService facevisaService) {
        this.facevisaService = facevisaService;
    }

    @Path("autoVisualInspection/silkCodes/{code}")
    @GET
    public Single<AutoVisualInspectionSilkInfoDTO> token(@PathParam("code") String code) {
        log.info("test[" + code + "]");
        return facevisaService.autoVisualInspection_silkInfo(code);
    }
}
