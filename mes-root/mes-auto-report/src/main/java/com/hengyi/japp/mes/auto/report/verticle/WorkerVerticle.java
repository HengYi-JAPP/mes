package com.hengyi.japp.mes.auto.report.verticle;

import com.hengyi.japp.mes.auto.application.query.SilkCarRecordQuery;
import com.hengyi.japp.mes.auto.report.PackagePlanService;
import com.hengyi.japp.mes.auto.report.application.*;
import com.hengyi.japp.mes.auto.report.application.dto.silk_car_record.DoffingSilkCarRecordReport;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.StatisticReportDay;
import com.hengyi.japp.mes.auto.report.application.dto.statistic.StatisticReportRange;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.reactivex.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Collection;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;

//import com.hengyi.japp.mes.auto.report.application.StrippingReportService;

/**
 * @author jzb 2019-05-20
 */
@Slf4j
public class WorkerVerticle extends AbstractVerticle {
    private QueryService queryService = INJECTOR.getInstance(QueryService.class);
    private DyeingReportService dyeingReportService = INJECTOR.getInstance(DyeingReportService.class);
    //    private StrippingReportService strippingReportService = INJECTOR.getInstance(StrippingReportService.class);
    private MeasureFiberReportService measureFiberReportService = INJECTOR.getInstance(MeasureFiberReportService.class);
    private SilkExceptionReportService silkExceptionReportService = INJECTOR.getInstance(SilkExceptionReportService.class);
    private PackagePlanService packagePlanService = INJECTOR.getInstance(PackagePlanService.class);

    @Override
    public Completable rxStart() {
        return Completable.mergeArray(
                vertx.eventBus().consumer("mes-auto:report:dyeingReport", dyeingReportService::dyeingReport).rxCompletionHandler(),
//                vertx.eventBus().consumer("mes-auto:report:strippingReport", strippingReportService::strippingReport).rxCompletionHandler(),
                vertx.eventBus().consumer("mes-auto:report:measureFiberReport", measureFiberReportService::measureFiberReport).rxCompletionHandler(),
                vertx.eventBus().consumer("mes-auto:report:silkExceptionReport", silkExceptionReportService::silkExceptionReport).rxCompletionHandler(),
                vertx.eventBus().consumer("mes-auto:report:packagePlanBoard", packagePlanService::packagePlanBoard).rxCompletionHandler(),

                vertx.eventBus().<String>consumer("mes-auto:report:doffingSilkCarRecordReport", reply -> Single.just(reply.body()).map(MAPPER::readTree).map(jsonNode -> {
                            final SilkCarRecordQuery silkCarRecordQuery = SilkCarRecordQuery.builder()
                                    .workshopId(jsonNode.get("workshopId").asText(null))
                                    .startDate(LocalDate.parse(jsonNode.get("startDate").asText()))
                                    .endDate(LocalDate.parse(jsonNode.get("endDate").asText()))
                                    .build();
                            final Collection<String> silkCarRecordIds = queryService.query(silkCarRecordQuery);
                            final DoffingSilkCarRecordReport doffingSilkCarRecordReport = new DoffingSilkCarRecordReport(silkCarRecordIds);
                            return MAPPER.writeValueAsString(doffingSilkCarRecordReport.toJsonNode());
                        }).subscribe(reply::reply, err -> {
                            log.error("", err);
                            reply.fail(400, err.getLocalizedMessage());
                        })
                ).rxCompletionHandler(),

                vertx.eventBus().<String>consumer("mes-auto:report:statisticReport:generate", reply -> Single.just(reply.body()).map(MAPPER::readTree).map(jsonNode -> {
                            final StatisticReportService statisticReportService = INJECTOR.getInstance(StatisticReportService.class);
                            final String workshopId = jsonNode.get("workshopId").asText(null);
                            final LocalDate ld = LocalDate.parse(jsonNode.get("date").asText(null));
                            final StatisticReportDay report = statisticReportService.generate(workshopId, ld).block();
                            return MAPPER.writeValueAsString(report);
                        }).subscribe(reply::reply, err -> {
                            log.error("", err);
                            reply.fail(400, err.getLocalizedMessage());
                        })
                ).rxCompletionHandler(),

                vertx.eventBus().<String>consumer("mes-auto:report:statisticReport:fromDisk", reply -> Single.just(reply.body()).map(MAPPER::readTree).map(jsonNode -> {
                            final StatisticReportService statisticReportService = INJECTOR.getInstance(StatisticReportService.class);
                            final String workshopId = jsonNode.get("workshopId").asText(null);
                            final LocalDate ld = LocalDate.parse(jsonNode.get("date").asText(null));
                            final StatisticReportDay report = statisticReportService.fromDisk(workshopId, ld).block();
                            return MAPPER.writeValueAsString(report);
                        }).subscribe(reply::reply, err -> {
                            log.error("", err);
                            reply.fail(400, err.getLocalizedMessage());
                        })
                ).rxCompletionHandler(),

                vertx.eventBus().<String>consumer("mes-auto:report:statisticReport:rangeDisk", reply -> Single.just(reply.body()).map(MAPPER::readTree).map(jsonNode -> {
                            final StatisticReportService statisticReportService = INJECTOR.getInstance(StatisticReportService.class);
                            final String workshopId = jsonNode.get("workshopId").asText(null);
                            final LocalDate startLd = LocalDate.parse(jsonNode.get("startDate").asText(null));
                            final LocalDate endLd = LocalDate.parse(jsonNode.get("endDate").asText(null));
                            final StatisticReportRange report = statisticReportService.rangeDisk(workshopId, startLd, endLd);
                            return MAPPER.writeValueAsString(report);
                        }).subscribe(reply::reply, err -> {
                            log.error("", err);
                            reply.fail(400, err.getLocalizedMessage());
                        })
                ).rxCompletionHandler()
        );
    }

}
