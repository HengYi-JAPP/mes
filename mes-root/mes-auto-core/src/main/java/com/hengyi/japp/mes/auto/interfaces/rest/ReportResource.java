package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.ReportCommand;
import com.hengyi.japp.mes.auto.application.report.WorkshopProductPlanReport;
import com.hengyi.japp.mes.auto.repository.LineMachineRepository;
import com.hengyi.japp.mes.auto.repository.LineRepository;
import com.hengyi.japp.mes.auto.repository.WorkshopRepository;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api/reports")
@Produces(APPLICATION_JSON)
public class ReportResource {
    private final Vertx vertx;
    private final WorkshopRepository workshopRepository;
    private final LineRepository lineRepository;
    private final LineMachineRepository lineMachineRepository;

    @Inject
    private ReportResource(Vertx vertx, WorkshopRepository workshopRepository, LineRepository lineRepository, LineMachineRepository lineMachineRepository) {
        this.vertx = vertx;
        this.workshopRepository = workshopRepository;
        this.lineRepository = lineRepository;
        this.lineMachineRepository = lineMachineRepository;
    }

    @Path("workshopProductPlanReport")
    @GET
    public Single<WorkshopProductPlanReport> workshopProductPlanReport(@QueryParam("workshopId") @NotBlank String workshopId) {
        return workshopRepository.find(workshopId)
                .flatMapPublisher(lineRepository::listBy)
                .flatMap(lineMachineRepository::listBy).toList()
                .map(WorkshopProductPlanReport::new);
    }

    @Path("measurePackageBoxReport")
    @POST
    public Single<String> measurePackageBoxReport(ReportCommand command) {
        return vertx.eventBus().<String>rxSend("mes-auto:report:measurePackageBoxReport", command).map(Message::body);
    }

//    @Path("doffingSilkCarRecordReport")
//    @GET
//    public Single<String> doffingSilkCarRecordReport(@QueryParam("workshopId") @NotBlank String workshopId,
//                                                     @QueryParam("startDate") @NotBlank String startDateString,
//                                                     @QueryParam("endDate") @NotBlank String endDateString) {
//        final JsonObject message = new JsonObject().put("workshopId", workshopId).put("startDate", startDateString).put("endDate", endDateString);
//        final DeliveryOptions deliveryOptions = new DeliveryOptions().setSendTimeout(Duration.ofMinutes(5).toMillis());
//        return vertx.eventBus().<String>rxSend("mes-auto:report:doffingSilkCarRecordReport", message.encode(), deliveryOptions).map(Message::body);
//    }

}
