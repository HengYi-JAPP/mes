package com.hengyi.japp.mes.auto.report.application.internal;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQuery;
import com.hengyi.japp.mes.auto.application.query.SilkCarRecordQuery;
import com.hengyi.japp.mes.auto.application.query.SilkCarRecordQueryByEventSourceCanHappen;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.interfaces.search.SearchService;
import com.hengyi.japp.mes.auto.report.application.QueryService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * @author jzb 2019-05-20
 */
@Singleton
public class QueryServiceImpl implements QueryService {
    private final MesAutoConfig mesAutoConfig;
    private final SearchService searchService;

    @Inject
    private QueryServiceImpl(MesAutoConfig mesAutoConfig, SearchService searchService) {
        this.mesAutoConfig = mesAutoConfig;
        this.searchService = searchService;
    }

    @Override
    public Collection<String> querySilkCarRecordIds(String workshopId, long startL, long endL) {
        final Map<Object, Object> map = Maps.newHashMap();
        final SilkCarRecordQuery query = SilkCarRecordQuery.builder()
                .pageSize(Integer.MAX_VALUE)
                .workshopId(workshopId)
                .startDateTime(new Date(startL))
                .endDateTime(new Date(endL))
                .build();
        return searchService.query(query).getRight();
    }

    @Override
    public Collection<String> querySilkCarRecordIdsByEventSourceCanHappen(String workshopId, long startL, long endL) {
        final SilkCarRecordQueryByEventSourceCanHappen query = SilkCarRecordQueryByEventSourceCanHappen.builder()
                .workshopId(workshopId)
                .startDateTime(new Date(startL))
                .endDateTime(new Date(endL))
                .build();
        return searchService.query(query);
    }

    @Override
    public Collection<String> queryPackageBoxIds(String workshopId, LocalDate startBudat, LocalDate endBudat) {
        final PackageBoxQuery query = PackageBoxQuery.builder().pageSize(Integer.MAX_VALUE)
                .workshopId(workshopId)
                .startBudat(startBudat)
                .endBudat(endBudat.plusDays(1))
                .build();
        return searchService.query(query).getRight();
    }

    @Override
    public Collection<String> queryPackageBoxIds(String workshopId, long startDateTime, long endDateTime) {
        return Collections.EMPTY_LIST;
    }
}
