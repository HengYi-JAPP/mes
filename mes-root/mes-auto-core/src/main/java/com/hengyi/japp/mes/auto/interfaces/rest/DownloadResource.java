package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.ReportService;
import com.hengyi.japp.mes.auto.application.report.MeasureReport;
import io.reactivex.Single;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api/downloads")
@Produces(APPLICATION_OCTET_STREAM)
public class DownloadResource {
    private final ReportService reportService;

    @Inject
    private DownloadResource(ReportService reportService) {
        this.reportService = reportService;
    }

    @Path("reports/measureReport")
    @POST
    public Single<MeasureReport> measureReport(MeasureReport.Command command) {
        return reportService.measureReport(command);
    }

}
