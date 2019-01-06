package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.PackageBoxService;
import com.hengyi.japp.mes.auto.application.ReportService;
import com.hengyi.japp.mes.auto.application.report.WorkshopProductPlanReport;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.Product;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.repository.LineRepository;
import com.hengyi.japp.mes.auto.repository.PackageBoxRepository;
import com.hengyi.japp.mes.auto.repository.ProductRepository;
import com.hengyi.japp.mes.auto.repository.WorkshopRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.redis.RedisClient;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("share")
@Produces(APPLICATION_JSON)
public class ShareResource {
    private final RedisClient redisClient;
    private final ReportService reportService;
    private final ProductRepository productRepository;
    private final WorkshopRepository workshopRepository;
    private final LineRepository lineRepository;
    private final PackageBoxRepository packageBoxRepository;

    @Inject
    private ShareResource(RedisClient redisClient, ReportService reportService, ProductRepository productRepository, WorkshopRepository workshopRepository, LineRepository lineRepository, PackageBoxRepository packageBoxRepository) {
        this.redisClient = redisClient;
        this.reportService = reportService;
        this.productRepository = productRepository;
        this.workshopRepository = workshopRepository;
        this.lineRepository = lineRepository;
        this.packageBoxRepository = packageBoxRepository;
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
        return reportService.workshopProductPlanReport(workshopId, lineId);
    }
}
