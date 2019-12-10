package com.hengyi.japp.mes.auto.report.application;

import com.google.inject.Inject;
import io.vertx.reactivex.core.eventbus.Message;

/**
 * @author liuyuan
 * @create 2019-05-29 18:16
 * @description
 **/
public class DyeingReportService {

    private final QueryService queryService;

    @Inject
    public DyeingReportService(QueryService queryService) {
        this.queryService = queryService;
    }

    public void dyeingReport(Message<Object> reply) {
    }
}
