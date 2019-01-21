package com.hengyi.japp.mes.auto.application;

import io.reactivex.Single;

import java.time.LocalDate;

/**
 * @author jzb 2018-06-22
 */
public interface DownloadService {

    Single<byte[]> test();

    Single<byte[]> statisticsReport(String workshopId, LocalDate ldStart, LocalDate ldEnd);

}
