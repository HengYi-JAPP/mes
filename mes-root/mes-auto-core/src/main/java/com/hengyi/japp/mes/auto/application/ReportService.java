package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.application.report.*;
import io.reactivex.Single;

import java.time.LocalDate;

/**
 * @author jzb 2018-06-22
 */
public interface ReportService {

    Single<MeasurePackageBoxReport> measurePackageBoxReport(String workshopId, LocalDate startLd, LocalDate endLd, String budatClassId);

    Single<MeasureReport> measureReport(String workshopId, String budatClassId, LocalDate parse);

    Single<StatisticsReport> statisticsReport(String workshopId, LocalDate startLd, LocalDate endLd);

    Single<WorkshopProductPlanReport> workshopProductPlanReport(String workshopId, String lineId);

    Single<DoffingReport> doffingReport(String workshopId, LocalDate ldStart, LocalDate ldEnd);

    default Single<DoffingReport> doffingReport(String workshopId, LocalDate ldStart) {
        return doffingReport(workshopId, ldStart, ldStart.plusDays(1));
    }

    Single<PackageBoxReport> packageBoxReport(String workshopId, LocalDate ldStart, LocalDate ldEnd);

    default Single<PackageBoxReport> packageBoxReport(String workshopId, LocalDate ldStart) {
        return packageBoxReport(workshopId, ldStart, ldStart.plusDays(1));
    }

    Single<SilkExceptionReport> silkExceptionReport(String workshopId, LocalDate ldStart, LocalDate ldEnd);

    default Single<SilkExceptionReport> silkExceptionReport(String workshopId, LocalDate ldStart) {
        return silkExceptionReport(workshopId, ldStart, ldStart.plusDays(1));
    }
}
