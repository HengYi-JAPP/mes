package com.hengyi.japp.mes.auto.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.ReportCommand;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.application.event.ProductProcessSubmitEvent;
import com.hengyi.japp.mes.auto.application.query.LocalDateRange;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQuery;
import com.hengyi.japp.mes.auto.application.query.SilkCarRecordByWorkshopQuery;
import com.hengyi.japp.mes.auto.application.query.SilkQuery;
import com.hengyi.japp.mes.auto.application.report.MeasurePackageBoxReport;
import com.hengyi.japp.mes.auto.application.report.SilkExceptionReport;
import com.hengyi.japp.mes.auto.application.report.StrippingReport;
import com.hengyi.japp.mes.auto.application.report.WorkshopProductPlanReport;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api/reports")
@Produces(APPLICATION_JSON)
public class ReportResource {
    private final WorkshopRepository workshopRepository;
    private final LineRepository lineRepository;
    private final LineMachineRepository lineMachineRepository;
    private final PackageBoxRepository packageBoxRepository;
    private final SilkRepository silkRepository;
    private final SilkCarRecordRepository silkCarRecordRepository;
    private final DyeingPrepareRepository dyeingPrepareRepository;
    private final SilkCarRuntimeRepository silkCarRuntimeRepository;

    @Inject
    public ReportResource(WorkshopRepository workshopRepository, LineRepository lineRepository, LineMachineRepository lineMachineRepository, PackageBoxRepository packageBoxRepository, SilkRepository silkRepository, SilkCarRecordRepository silkCarRecordRepository, DyeingPrepareRepository dyeingPrepareRepository, SilkCarRuntimeRepository silkCarRuntimeRepository) {
        this.workshopRepository = workshopRepository;
        this.lineRepository = lineRepository;
        this.lineMachineRepository = lineMachineRepository;
        this.packageBoxRepository = packageBoxRepository;
        this.silkRepository = silkRepository;
        this.silkCarRecordRepository = silkCarRecordRepository;
        this.dyeingPrepareRepository = dyeingPrepareRepository;
        this.silkCarRuntimeRepository = silkCarRuntimeRepository;
    }

    @Path("workshopProductPlanReport")
    @GET
    public Single<WorkshopProductPlanReport> workshopProductPlanReport(@QueryParam("workshopId") String workshopId,
                                                                       @QueryParam("lineId") String lineId) {
        final Flowable<Line> lines$;
        if (J.nonBlank(lineId)) {
            lines$ = lineRepository.find(lineId).toFlowable();
        } else {
            final Single<Workshop> workshop$ = J.nonBlank(workshopId)
                    ? workshopRepository.find(workshopId)
                    : workshopRepository.list().firstOrError();
            lines$ = workshop$.flatMapPublisher(lineRepository::listBy);
        }
        return lines$.flatMap(lineMachineRepository::listBy).toList()
                .map(WorkshopProductPlanReport::new);
    }

    @Path("measurePackageBoxReport")
    @POST
    public Single<MeasurePackageBoxReport> measurePackageBoxReport(ReportCommand command) {
        final LocalDate startLd = J.localDate(command.getStartDate());
        final LocalDate endLd = J.localDate(command.getEndDate());
        final Set<@NotBlank String> budatClassIds = J.emptyIfNull(command.getPackageClasses()).stream().map(EntityDTO::getId).collect(toSet());
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .workshopId(command.getWorkshop().getId())
                .budatRange(new LocalDateRange(startLd, endLd.plusDays(1)))
                .budatClassIds(budatClassIds)
                .build();
        return packageBoxRepository.query(packageBoxQuery).map(it -> {
            final Collection<PackageBox> packageBoxes = it.getPackageBoxes();
            return new MeasurePackageBoxReport(packageBoxes);
        });
    }

    @Path("strippingReport")
    @GET
    public Single<StrippingReport> strippingReport(@QueryParam("workshopId") String workshopId,
                                                   @QueryParam("startDate") @NotBlank String startLdString,
                                                   @QueryParam("endDate") @NotBlank String endLdString) {
        SilkCarRecordByWorkshopQuery silkCarRecordByWorkshopQuery = SilkCarRecordByWorkshopQuery.builder()
                .startDate(LocalDate.parse(startLdString))
                .endDate(LocalDate.parse(endLdString)).build();
        return silkCarRecordRepository.listByWorkshop(silkCarRecordByWorkshopQuery)
                .flatMap(id -> silkCarRecordRepository.find(id)
                        .flatMapPublisher(silkCarRecord -> {
                            final String eventsJsonString = silkCarRecord.getEventsJsonString();
                            final String initEventsJsonString = silkCarRecord.getInitEventJsonString();
                            if (J.isBlank(eventsJsonString)) {
                                return Flowable.empty();
                            }
                            final JsonNode eventsArrayNode = MAPPER.readTree(eventsJsonString);
                            final JsonNode initEventNode = MAPPER.readTree(initEventsJsonString);
                            ArrayNode arrayNode = (ArrayNode) eventsArrayNode;
                            arrayNode.add(initEventNode);
                            JsonNode jsonNode = arrayNode;
                            return Flowable.fromIterable(jsonNode)
                                    .flatMapSingle(EventSource::from);
                        })).filter(eventSource -> {
//                    if (EventSourceType.SilkCarRuntimeInitEvent.equals(eventSource.getType())){
//                        return true;
//                    } else
                    if (EventSourceType.ProductProcessSubmitEvent.equals(eventSource.getType())) {
                        ProductProcessSubmitEvent productProcessSubmitEvent = (ProductProcessSubmitEvent) eventSource;
                        return "剥丝".equals(productProcessSubmitEvent.getProductProcess().getName());
                    }
                    return false;
                })
                .toList()
                .map(list -> new StrippingReport(list));
    }

//    @Path("measureFiberReport")
//    @GET
//    public Single<MeasureFiberReport> measureFiberReport(@QueryParam("workshopId") String workshopId,
//                                                         @QueryParam("startDate") @NotBlank String startLdString,
//                                                         @QueryParam("endDate") @NotBlank String endLdString) {
//        SilkCarRecordQuery silkCarRecordQuery = SilkCarRecordQuery.builder()
//                .pageSize(Integer.MAX_VALUE)
//                .workShopId(workshopId)
//                .startDate(LocalDate.parse(startLdString))
//                .endDate(LocalDate.parse(endLdString))
//                .build();
//        return silkCarRecordRepository
//                .query(silkCarRecordQuery)
//                .flatMap(result -> Single.just(result.getSilkCarRecords()))
//                .flatMapPublisher(Flowable::fromIterable)
//                .flatMap(silkCarRecord -> silkCarRuntimeRepository.findByCode(silkCarRecord.getSilkCar().getCode())
//                        .flatMapPublisher(silkCarRuntime -> {
//                            if (silkCarRuntime.getSilkCarRecord() != null && silkCarRecord.getId().equals(silkCarRuntime.getSilkCarRecord().getId())) {
//                                return Flowable.fromIterable(silkCarRuntime.getEventSources())
//                                        .toList()
//                                        .map(list -> new MeasureFiberReport.Item(list, silkCarRecord, silkCarRecord.getBatch().getProduct()))
//                                        .flatMapPublisher(Flowable::just);
//
//                            } else {
//                                final String eventsJsonString = silkCarRecord.getEventsJsonString();
//                                if (J.isBlank(eventsJsonString)) {
//                                    return Flowable.empty();
//                                }
//                                final JsonNode jsonNode = MAPPER.readTree(eventsJsonString);
//                                return Flowable.fromIterable(jsonNode)
//                                        .flatMapSingle(EventSource::from)
//                                        .toList()
//                                        .map(list -> new MeasureFiberReport.Item(list, silkCarRecord, silkCarRecord.getBatch().getProduct()))
//                                        .flatMapPublisher(Flowable::just);
//                            }
//                        })
//                )
//                .filter(item -> item.getEventSources().parallelStream().anyMatch(eventSource ->
//                        EventSourceType.SilkNoteFeedbackEvent.equals(eventSource.getType()) && "测纤".equals(((SilkNoteFeedbackEvent) eventSource).getSilkNote().getName())))
//                .toList()
//                .map(MeasureFiberReport::new);
//    }

    @Path("dailySilkExceptionReport")
    @GET
    public Single<SilkExceptionReport> dailySilkExceptionReport(@QueryParam("workshopId") @NotBlank String workshopId,
                                                                @QueryParam("startDateTime") long startDateTime,
                                                                @QueryParam("endDateTime") long endDateTime) {
        final SilkQuery silkQuery = SilkQuery.builder()
                .workshopId(workshopId)
                .ldtStart(startDateTime)
                .ldtEnd(endDateTime)
                .pageSize(Integer.MAX_VALUE)
                .build();
        return silkRepository.query(silkQuery)
                .map(it -> new SilkExceptionReport(it.getSilks()));
    }

}
