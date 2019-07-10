package com.hengyi.japp.mes.auto.report.application.dto.silk_car_record;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import lombok.Getter;
import org.bson.Document;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;

/**
 * @author jzb 2019-07-11
 */
public abstract class SilkCarRecordAggregate implements Serializable {
    @JsonIgnore
    protected final Document document;
    @Getter
    protected final String id;
    @JsonIgnore
    @Getter
    protected final String doffingOperatorId;
    @Getter
    protected final Date doffingDateTime;
    @Getter
    protected final Date endDateTime;
    @Getter
    private final Document batch;
    @Getter
    private final Document grade;
    @Getter(lazy = true)
    private final Collection<SilkRuntime> initSilkRuntimes = fetchInitSilkRuntimes();

    protected SilkCarRecordAggregate(Document document) {
        this.document = document;
        id = document.getString(ID_COL);
        doffingDateTime = document.getDate("doffingDateTime");
        endDateTime = document.getDate("endDateTime");
        doffingOperatorId = document.getString("doffingOperatorId");
        batch = QueryService.findFromCache(Batch.class, document.getString("batch")).get();
        grade = QueryService.findFromCache(Grade.class, document.getString("grade")).get();
    }

    public static SilkCarRecordAggregate from(String id) {
        final Document document = QueryService.find(SilkCarRecord.class, id).block();
        return document.getDate("endDateTime") != null ? new SilkCarRecordAggregate_History(document) : new SilkCarRecordAggregate_Runtime(document);
    }

    protected abstract Collection<SilkRuntime> fetchInitSilkRuntimes();

}
