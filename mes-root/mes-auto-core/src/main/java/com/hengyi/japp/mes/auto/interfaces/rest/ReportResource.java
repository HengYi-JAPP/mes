package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.ReportService;
import com.hengyi.japp.mes.auto.application.report.MeasurePackageBoxReport;
import com.hengyi.japp.mes.auto.application.report.MeasureReport;
import com.hengyi.japp.mes.auto.application.report.StatisticsReport;
import com.hengyi.japp.mes.auto.application.report.WorkshopProductPlanReport;
import io.reactivex.Single;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.time.LocalDate;

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

    @Path("statisticsReport")
    @POST
    public Single<StatisticsReport> measureReport(StatisticsReport.Command command) {
        return reportService.statisticsReport(command);
    }

    @Path("measureReport")
    @POST
    public Single<MeasureReport> measureReport(MeasureReport.Command command) {
        return reportService.measureReport(command);
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
