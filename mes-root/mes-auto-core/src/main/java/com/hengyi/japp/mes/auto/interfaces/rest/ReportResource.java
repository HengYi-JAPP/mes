package com.hengyi.japp.mes.auto.interfaces.rest;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.ReportService;
import com.hengyi.japp.mes.auto.application.report.MeasurePackageBoxReport;
import com.hengyi.japp.mes.auto.application.report.MeasureReport;
import com.hengyi.japp.mes.auto.application.report.StatisticsReport;
import com.hengyi.japp.mes.auto.application.report.WorkshopProductPlanReport;
import io.reactivex.Single;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.time.LocalDate;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api/reports")
@Produces(APPLICATION_JSON)
public class ReportResource {
    private final ReportService reportService;

    @Inject
    private ReportResource(ReportService reportService) {
        this.reportService = reportService;
    }

    @Path("workshopProductPlanReport")
    @GET
    public Single<WorkshopProductPlanReport> workshopProductPlanReport(@QueryParam("workshopId") String workshopId,
                                                                       @QueryParam("lineId") String lineId) {
        return reportService.workshopProductPlanReport(workshopId, lineId);
    }

    @Path("measurePackageBoxReport")
    @GET
    public Single<MeasurePackageBoxReport> measurePackageBoxReport(@QueryParam("date") @NotBlank String dateString,
                                                                   @QueryParam("budatClassId") @NotBlank String budatClassId) {
        return reportService.measurePackageBoxReport(LocalDate.parse(dateString), budatClassId);
    }

    @Path("measureReport")
    @GET
    public Single<MeasureReport> measureReport(@QueryParam("workshopId") String workshopId,
                                               @QueryParam("budatClassId") String budatClassId,
                                               @QueryParam("date") @NotBlank String dateString) {
        final LocalDate ld = Optional.ofNullable(dateString)
                .filter(J::nonBlank)
                .map(LocalDate::parse)
                .orElse(LocalDate.now());
        return reportService.measureReport(workshopId, budatClassId, ld);
    }

    @Path("statisticsReport")
    @GET
    public Single<StatisticsReport> statisticsReport(@QueryParam("workshopId") String workshopId,
                                                     @QueryParam("startDate") @NotBlank String startLdString,
                                                     @QueryParam("endDate") @NotBlank String endLdString) {
        return reportService.statisticsReport(workshopId, LocalDate.parse(startLdString), LocalDate.parse(endLdString));
    }

//    @Path("dailyDoffingReport")
//    @GET
//    public Single<DoffingReport> dailyDoffingReport(@QueryParam("workshopId") @NotBlank String workshopId,
//                                                    @QueryParam("date") @NotBlank String dateString) {
//        return reportService.doffingReport(workshopId, LocalDate.parse(dateString));
//    }
//
//    @Path("dailyPackageBoxReport")
//    @GET
//    public Single<PackageBoxReport> dailyPackageBoxReport(@QueryParam("workshopId") @NotBlank String workshopId,
//                                                          @QueryParam("date") @NotBlank String dateString) {
//        return reportService.packageBoxReport(workshopId, LocalDate.parse(dateString));
//    }
//
//    @Path("monthPackageBoxReport")
//    @GET
//    public Single<PackageBoxReport> monthPackageBoxReport(@QueryParam("workshopId") @NotBlank String workshopId,
//                                                          @QueryParam("year") @Min(1) int year,
//                                                          @QueryParam("month") @Min(1) @Max(12) int month) {
//        final LocalDate ldStart = LocalDate.of(year, month, 1);
//        final LocalDate ldEnd = ldStart.plusMonths(1);
//        return reportService.packageBoxReport(workshopId, ldStart, ldEnd);
//    }
//
//    @Path("dailySilkExceptionReport")
//    @GET
//    public Single<SilkExceptionReport> dailySilkExceptionReport(@QueryParam("workshopId") @NotBlank String workshopId,
//                                                                @QueryParam("date") @NotBlank String dateString) {
//        return reportService.silkExceptionReport(workshopId, LocalDate.parse(dateString));
//    }
//
//    @Path("monthSilkExceptionReport")
//    @GET
//    public Single<SilkExceptionReport> monthSilkExceptionReport(@QueryParam("workshopId") @NotBlank String workshopId,
//                                                                @QueryParam("year") @Min(1) int year,
//                                                                @QueryParam("month") @Min(1) @Max(12) int month) {
//        final LocalDate ldStart = LocalDate.of(year, month, 1);
//        final LocalDate ldEnd = ldStart.plusMonths(1);
//        return reportService.silkExceptionReport(workshopId, ldStart, ldEnd);
//    }
}
