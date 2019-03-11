package com.hengyi.japp.mes.auto.interfaces.riamb;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.interfaces.riamb.dto.RiambFetchSilkCarRecordResultDTO;
import com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambPackageBoxEvent;
import com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambSilkDetachEvent;
import io.reactivex.Completable;
import io.reactivex.Single;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.interfaces.riamb.RiambService.LOG;
import static com.hengyi.japp.mes.auto.interfaces.riamb.RiambService.PRINCIPAL;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2019-02-21
 */
@Singleton
@Path("riamb")
@Produces(APPLICATION_JSON)
public class RiambResource {
    private final RiambService riambService;

    @Inject
    private RiambResource(RiambService riambService) {
        this.riambService = riambService;
    }

    @Path("silkCarRecords/codes/{code}")
    @GET
    public Single<RiambFetchSilkCarRecordResultDTO> fetchSilkCarRecord(@PathParam("code") @NotBlank String code) {
        return riambService.fetchSilkCarRecord(PRINCIPAL, code)
                .doOnSuccess(it -> {
                    final StringBuilder sb = new StringBuilder("RiambResource.fetchSilkCarRecord: ").append(code)
                            .append("\n").append(MAPPER.writeValueAsString(it)).append("成功!");
                    LOG.info(sb.toString());
                })
                .doOnError(ex -> {
                    final StringBuilder sb = new StringBuilder("RiambResource.fetchSilkCarRecord: ").append(code)
                            .append("\n").append("失败!");
                    LOG.error(sb.toString(), ex);
                });
    }

    @Path("SilkDetachEvents")
    @POST
    public Completable fetchSilkCarRecord(RiambSilkDetachEvent.Command command) {
        return riambService.handle(PRINCIPAL, command)
                .doOnComplete(() -> {
                    final StringBuilder sb = new StringBuilder("RiambResource.SilkDetachEvents: ")
                            .append(MAPPER.writeValueAsString(command))
                            .append("\n").append("成功!");
                    LOG.info(sb.toString());
                })
                .doOnError(ex -> {
                    final StringBuilder sb = new StringBuilder("RiambResource.SilkDetachEvents: ")
                            .append(MAPPER.writeValueAsString(command))
                            .append("\n").append("失败!");
                    LOG.error(sb.toString(), ex);
                });
    }

    @Path("PackageBoxEvents")
    @POST
    public Completable fetchSilkCarRecord(RiambPackageBoxEvent.Command command) {
        return riambService.packageBox(PRINCIPAL, command)
                .doOnComplete(() -> {
                    final StringBuilder sb = new StringBuilder("RiambResource.PackageBoxEvents: ")
                            .append(MAPPER.writeValueAsString(command))
                            .append("\n").append("成功!");
                    LOG.info(sb.toString());
                })
                .doOnError(ex -> {
                    final StringBuilder sb = new StringBuilder("RiambResource.PackageBoxEvents: ")
                            .append(MAPPER.writeValueAsString(command))
                            .append("\n").append("失败!");
                    LOG.error(sb.toString(), ex);
                });
    }

}
