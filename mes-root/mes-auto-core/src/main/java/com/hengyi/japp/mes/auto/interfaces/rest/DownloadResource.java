package com.hengyi.japp.mes.auto.interfaces.rest;

import com.github.ixtf.japp.vertx.annotations.FileDownload;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.DownloadService;
import io.reactivex.Single;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

/**
 * @author jzb 2018-11-02
 */
@FileDownload
@Singleton
@Path("api/downloads")
@Produces(APPLICATION_OCTET_STREAM)
public class DownloadResource {
    private final DownloadService downloadService;

    @Inject
    private DownloadResource(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    // http://localhost:9999/api/downloads/reports/test
    @Path("reports/test")
    @GET
    public Single<FileDownload.DTO> test() {
        return downloadService.test().map(content -> {
            final FileDownload.DTO result = new FileDownload.DTO();
            result.setFileName("报表-测试-我们去.xlsx");
            result.setContent(content);
            return result;
        });
    }

    @Path("reports/statisticsReportDay")
    @GET
    public Single<FileDownload.DTO> statisticsReportDay(@QueryParam("workshopId") @NotBlank String workshopId,
                                                        @QueryParam("date") @NotBlank String dateString) {
        final LocalDate ld = LocalDate.parse(dateString);
        return downloadService.statisticsReport(workshopId, ld, ld).map(content -> {
            final FileDownload.DTO result = new FileDownload.DTO();
            result.setFileName("打包量统计日报表-[" + ld + "].xlsx");
            result.setContent(content);
            return result;
        });
    }

    @Path("reports/statisticsReportWeek")
    @GET
    public Single<FileDownload.DTO> statisticsReportWeek(@QueryParam("workshopId") @NotBlank String workshopId,
                                                         @QueryParam("date") @NotBlank String dateString) {
        final LocalDate ldStart = LocalDate.parse(dateString);
        if (DayOfWeek.MONDAY != ldStart.getDayOfWeek()) {
            throw new RuntimeException();
        }
        final LocalDate ldEnd = ldStart.plusDays(7);
        return downloadService.statisticsReport(workshopId, ldStart, ldEnd).map(content -> {
            final FileDownload.DTO result = new FileDownload.DTO();
            result.setFileName("打包量统计周报表-[" + ldStart + "]~[" + ldEnd + "].xlsx");
            result.setContent(content);
            return result;
        });
    }

    @Path("reports/statisticsReportMonth")
    @GET
    public Single<FileDownload.DTO> statisticsReportMonth(@QueryParam("workshopId") @NotBlank String workshopId,
                                                          @QueryParam("year") @Min(2019) int year,
                                                          @QueryParam("month") @Min(1) @Max(12) int month) {
        final LocalDate ldStart = LocalDate.of(year, month, 1);
        final LocalDate ldEnd = ldStart.plusMonths(1).plusDays(-1);
        return downloadService.statisticsReport(workshopId, ldStart, ldEnd).map(content -> {
            final FileDownload.DTO result = new FileDownload.DTO();
            result.setFileName("打包量统计月报表-[" + year + "-" + month + "].xlsx");
            result.setContent(content);
            return result;
        });
    }

    @Path("reports/statisticsReportYear")
    @GET
    public Single<FileDownload.DTO> statisticsReportYear(@QueryParam("workshopId") @NotBlank String workshopId,
                                                         @QueryParam("year") @Min(2019) int year) {
        final LocalDate ldStart = LocalDate.of(year, 1, 1);
        final LocalDate ldEnd = ldStart.plusYears(1).plusDays(-1);
        return downloadService.statisticsReport(workshopId, ldStart, ldEnd).map(content -> {
            final FileDownload.DTO result = new FileDownload.DTO();
            result.setFileName("打包量统计年报表-[" + year + "].xlsx");
            result.setContent(content);
            return result;
        });
    }

}
