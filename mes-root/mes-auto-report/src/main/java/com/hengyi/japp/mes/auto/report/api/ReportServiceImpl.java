package com.hengyi.japp.mes.auto.report.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.ReportService;
import com.hengyi.japp.mes.auto.application.command.ReportCommand;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.application.event.ProductProcessSubmitEvent;
import com.hengyi.japp.mes.auto.application.event.SilkNoteFeedbackEvent;
import com.hengyi.japp.mes.auto.application.query.*;
import com.hengyi.japp.mes.auto.application.report.*;
import com.hengyi.japp.mes.auto.domain.DyeingPrepare;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.toSet;

/**
 * @author jzb 2018-08-08
 */
@Slf4j
@Singleton
public class ReportServiceImpl implements ReportService {
    private final WorkshopRepository workshopRepository;
    private final LineRepository lineRepository;
    private final LineMachineRepository lineMachineRepository;
    private final PackageBoxRepository packageBoxRepository;
    private final SilkRepository silkRepository;
    private final SilkCarRecordRepository silkCarRecordRepository;
    private final DyeingPrepareRepository dyeingPrepareRepository;
    private final SilkCarRuntimeRepository silkCarRuntimeRepository;

    @Inject
    private ReportServiceImpl(WorkshopRepository workshopRepository, LineRepository lineRepository, LineMachineRepository lineMachineRepository, PackageBoxRepository packageBoxRepository, SilkRepository silkRepository, SilkCarRecordRepository silkCarRecordRepository, DyeingPrepareRepository dyeingPrepareRepository, SilkCarRuntimeRepository silkCarRuntimeRepository) {
        this.workshopRepository = workshopRepository;
        this.lineRepository = lineRepository;
        this.lineMachineRepository = lineMachineRepository;
        this.packageBoxRepository = packageBoxRepository;
        this.silkRepository = silkRepository;
        this.silkCarRecordRepository = silkCarRecordRepository;
        this.dyeingPrepareRepository = dyeingPrepareRepository;
        this.silkCarRuntimeRepository = silkCarRuntimeRepository;
    }

    @Override
    public Single<MeasureReport> measureReport(ReportCommand command) {
        final Set<@NotBlank String> budatClassIds = J.emptyIfNull(command.getPackageClasses()).stream().map(EntityDTO::getId).collect(toSet());
        final LocalDate startLd = J.localDate(command.getStartDate());
        final LocalDate endLd = J.localDate(command.getEndDate());
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .workshopId(command.getWorkshop().getId())
                .budatClassIds(budatClassIds)
                .budatRange(new LocalDateRange(startLd, endLd.plusDays(1)))
                .build();
        return packageBoxRepository.query(packageBoxQuery).map(it -> {
            final Collection<PackageBox> packageBoxes = it.getPackageBoxes();
            return new MeasureReport(packageBoxes);
        });
    }

    @Override
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

    @Override
    public Single<StatisticsReport> statisticsReport(String workshopId, LocalDate startLd, LocalDate endLd) {
        final Single<Workshop> workshop$ = J.isBlank(workshopId) ? workshopRepository.list().firstOrError() : workshopRepository.find(workshopId);
        return workshop$.flatMap(workshop -> {
            final List<LocalDate> lds = Stream.iterate(startLd, d -> d.plusDays(1))
                    .limit(ChronoUnit.DAYS.between(startLd, endLd) + 1)
                    .collect(Collectors.toList());
            return Flowable.fromIterable(lds)
                    .flatMapSingle(ld -> statisticsReportDay(workshop, ld)).toList()
                    .map(days -> new StatisticsReport(workshop, startLd, endLd, days));
//            return Flowable.fromIterable(lds)
//                    .parallel(7)
//                    .flatMap(ld -> statisticsReportDay(workshop, ld).toFlowable())
//                    .sequential()
//                    .toList()
//                    .map(days -> new StatisticsReport(workshop, startLd, endLd, days));
        });
    }

    /**
     * 剥丝报表
     *
     * @param workshopId
     * @param startLd
     * @param endLd
     * @return
     */
    @Override
    public Single<StrippingReport> strippingReport(String workshopId, LocalDate startLd, LocalDate endLd) {
        SilkCarRecordByWorkshopQuery silkCarRecordByWorkshopQuery = SilkCarRecordByWorkshopQuery.builder()
                .startDate(startLd)
                .endDate(endLd).build();
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

    @Override
    public Single<DyeingReport> dyeingReport(String workshopId, long startDateTimestamp, long endDateTimestamp) {
        final DyeingPrepareReportQuery dyeingPrepareReportQuery = DyeingPrepareReportQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .workshopId(workshopId)
                .startDateTimestamp(startDateTimestamp)
                .endDateTimestamp(endDateTimestamp)
                .build();
        return dyeingPrepareRepository.query(dyeingPrepareReportQuery).map(it -> {
            final Collection<DyeingPrepare> dyeingPrepares = it.getDyeingPrepares();
            return new DyeingReport(dyeingPrepares);
        });
    }

    @Override
    public Single<MeasureFiberReport> measureFiberReport(String workshopId, LocalDate startLd, LocalDate endLd) {
        SilkCarRecordQuery silkCarRecordQuery = SilkCarRecordQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .workShopId(workshopId)
                .startDate(startLd)
                .endDate(endLd)
                .build();
        return silkCarRecordRepository
                .query(silkCarRecordQuery)
                .flatMap(result -> Single.just(result.getSilkCarRecords()))
                .flatMapPublisher(Flowable::fromIterable)
                .flatMap(silkCarRecord -> silkCarRuntimeRepository.findByCode(silkCarRecord.getSilkCar().getCode())
                        .flatMapPublisher(silkCarRuntime -> {
                            if (silkCarRuntime.getSilkCarRecord() != null && silkCarRecord.getId().equals(silkCarRuntime.getSilkCarRecord().getId())) {
                                return Flowable.fromIterable(silkCarRuntime.getEventSources())
                                        .toList()
                                        .map(list -> new MeasureFiberReport.Item(list, silkCarRecord, silkCarRecord.getBatch().getProduct()))
                                        .flatMapPublisher(Flowable::just);

                            } else {
                                final String eventsJsonString = silkCarRecord.getEventsJsonString();
                                if (J.isBlank(eventsJsonString)) {
                                    return Flowable.empty();
                                }
                                final JsonNode jsonNode = MAPPER.readTree(eventsJsonString);
                                return Flowable.fromIterable(jsonNode)
                                        .flatMapSingle(EventSource::from)
                                        .toList()
                                        .map(list -> new MeasureFiberReport.Item(list, silkCarRecord, silkCarRecord.getBatch().getProduct()))
                                        .flatMapPublisher(Flowable::just);
                            }
                        })
                )
                .filter(item -> item.getEventSources().parallelStream().anyMatch(eventSource ->
                        EventSourceType.SilkNoteFeedbackEvent.equals(eventSource.getType()) && "测纤".equals(((SilkNoteFeedbackEvent) eventSource).getSilkNote().getName())))
                .toList()
                .map(MeasureFiberReport::new);
    }

    private Single<StatisticsReportDay> statisticsReportDay(Workshop workshop, LocalDate ld) {
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .workshopId(workshop.getId())
                .budatRange(new LocalDateRange(ld, ld.plusDays(1)))
                .build();
        return packageBoxRepository.query(packageBoxQuery).map(it -> {
            final Collection<PackageBox> packageBoxes = it.getPackageBoxes();
            return new StatisticsReportDay(workshop, ld, packageBoxes);
        });
    }

    @Override
    public Single<WorkshopProductPlanReport> workshopProductPlanReport(String workshopId, String lineId) {
        final Flowable<Line> lines$;
        if (StringUtils.isNotBlank(lineId)) {
            lines$ = lineRepository.find(lineId).toFlowable();
        } else {
            final Single<Workshop> workshop$ = StringUtils.isNotBlank(workshopId)
                    ? workshopRepository.find(workshopId)
                    : workshopRepository.list().firstOrError();
            lines$ = workshop$.flatMapPublisher(lineRepository::listBy);
        }
        return lines$.flatMap(lineMachineRepository::listBy).toList()
                .map(WorkshopProductPlanReport::new);
    }

    @Override
    public Single<DoffingReport> doffingReport(String workshopId, LocalDate ldStart, LocalDate ldEnd) {
        final SilkQuery silkQuery = SilkQuery.builder()
                .workshopId(workshopId)
                .ldStart(ldStart)
                .ldEnd(ldEnd)
                .pageSize(Integer.MAX_VALUE)
                .build();
        return silkRepository.query(silkQuery).map(it -> new DoffingReport(it.getSilks()));
    }

    @Override
    public Single<DoffingReport> doffingReport(String workshopId, LocalDate ldStart) {
        return null;
    }

    @Override
    public Single<PackageBoxReport> packageBoxReport(String workshopId, LocalDate ldStart, LocalDate ldEnd) {
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .workshopId(workshopId)
                .budatRange(new LocalDateRange(ldStart, ldEnd))
                .pageSize(Integer.MAX_VALUE)
                .build();
        return packageBoxRepository.query(packageBoxQuery)
                .map(it -> new PackageBoxReport(it.getPackageBoxes()));
    }

    @Override
    public Single<PackageBoxReport> packageBoxReport(String workshopId, LocalDate ldStart) {
        return null;
    }

    @Override
    public Single<SilkExceptionReport> silkExceptionReport(String workshopId, LocalDate ldStart, LocalDate ldEnd) {
        final SilkQuery silkQuery = SilkQuery.builder()
                .workshopId(workshopId)
                .ldStart(ldStart)
                .ldEnd(ldEnd)
                .pageSize(Integer.MAX_VALUE)
                .build();
        return silkRepository.query(silkQuery)
                .map(it -> new SilkExceptionReport(it.getSilks()));
    }

    @Override
    public Single<SilkExceptionReport> silkExceptionReport(String workshopId, LocalDate ldStart) {
        return null;
    }

}
