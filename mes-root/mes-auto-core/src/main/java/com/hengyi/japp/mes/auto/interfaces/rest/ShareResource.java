package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.ApplicationEvents;
import com.hengyi.japp.mes.auto.application.PackageBoxService;
import com.hengyi.japp.mes.auto.application.query.ExceptionRecordQuery;
import com.hengyi.japp.mes.auto.application.report.WorkshopProductPlanReport;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.redis.RedisClient;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("share")
@Produces(APPLICATION_JSON)
public class ShareResource {
    private final ApplicationEvents applicationEvents;
    private final RedisClient redisClient;
    private final ProductRepository productRepository;
    private final WorkshopRepository workshopRepository;
    private final LineRepository lineRepository;
    private final LineMachineRepository lineMachineRepository;
    private final PackageBoxRepository packageBoxRepository;
    private final ExceptionRecordRepository exceptionRecordRepository;
    private final NotificationRepository notificationRepository;
    private final SilkCarRuntimeRepository silkCarRuntimeRepository;

    @Inject
    private ShareResource(ApplicationEvents applicationEvents, RedisClient redisClient, ProductRepository productRepository, WorkshopRepository workshopRepository, LineRepository lineRepository, LineMachineRepository lineMachineRepository, PackageBoxRepository packageBoxRepository, ExceptionRecordRepository exceptionRecordRepository, NotificationRepository notificationRepository, SilkCarRuntimeRepository silkCarRuntimeRepository) {
        this.applicationEvents = applicationEvents;
        this.redisClient = redisClient;
        this.productRepository = productRepository;
        this.workshopRepository = workshopRepository;
        this.lineRepository = lineRepository;
        this.lineMachineRepository = lineMachineRepository;
        this.packageBoxRepository = packageBoxRepository;
        this.exceptionRecordRepository = exceptionRecordRepository;
        this.notificationRepository = notificationRepository;
        this.silkCarRuntimeRepository = silkCarRuntimeRepository;
    }

    @Path("packageBoxes/codes/{code}")
    @GET
    public Single<PackageBox> list(@PathParam("code") String code) {
        return packageBoxRepository.findByCode(code);
    }

    @Path("packageBoxes/dates/{date}/serial")
    @GET
    public Single<Long> serial(@PathParam("date") String dateString) {
        final LocalDate ld = LocalDate.parse(dateString);
        final long between = ChronoUnit.DAYS.between(LocalDate.now(), ld);
        if (Math.abs(between) >= 365) {
            throw new RuntimeException("时间超出");
        }
        final String incrKey = PackageBoxService.key(ld);
        return redisClient.rxIncr(incrKey);
    }

    @Path("silkCarRuntimes/{code}")
    @GET
    public Single<SilkCarRuntime> get(@PathParam("code") @NotBlank String code) {
        return silkCarRuntimeRepository.findByCode(code).toSingle(new SilkCarRuntime());
    }

    @Path("refreshAbnormalBoard")
    @GET
    public Completable refreshAbnormal() {
        return Completable.fromAction(applicationEvents::refreshAbnormalBoard);
    }

    @Path("refreshSilkCarRuntimeReportBoard")
    @GET
    public Completable refreshSilkCarRuntimeReport() {
        return Completable.fromAction(applicationEvents::refreshSilkCarRuntimeReportBoard);
    }

    @Path("workshops/{id}/productPlans")
    @GET
    public Single<WorkshopProductPlanReport> productPlans(@PathParam("id") @NotBlank String workshopId) {
        return workshopRepository.find(workshopId)
                .flatMapPublisher(lineRepository::listBy)
                .flatMap(lineMachineRepository::listBy).toList()
                .map(WorkshopProductPlanReport::new);
    }

    @Path("exceptionRecords")
    @GET
    public Single<Collection<ExceptionRecord>> exceptionRecords() {
        final ExceptionRecordQuery query = ExceptionRecordQuery.builder().build();
        return exceptionRecordRepository.query(query).map(ExceptionRecordQuery.Result::getResult);
    }

    @Path("notifications")
    @GET
    public Flowable<Notification> notifications() {
        return notificationRepository.list();
    }

    @Path("workshops")
    @GET
    public Flowable<Workshop> get() {
        return workshopRepository.list();
    }

    @Path("workshops/{id}/lines")
    @GET
    public Flowable<Line> lines(@PathParam("id") @NotBlank String id) {
        return lineRepository.listByWorkshopId(id);
    }

    @Path("products")
    @GET
    public Flowable<Product> query() {
        return productRepository.list();
    }

    @Path("reports/workshopProductPlanReport")
    @GET
    public Single<WorkshopProductPlanReport> workshopProductPlanReport(@QueryParam("lineId") String lineId,
                                                                       @QueryParam("workshopId") String workshopId) {
        return workshopRepository.find(workshopId)
                .flatMapPublisher(lineRepository::listBy)
                .flatMap(lineMachineRepository::listBy).toList()
                .map(WorkshopProductPlanReport::new);
    }
}
