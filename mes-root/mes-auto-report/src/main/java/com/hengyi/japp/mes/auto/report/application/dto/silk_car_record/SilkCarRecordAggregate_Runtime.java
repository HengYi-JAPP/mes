package com.hengyi.japp.mes.auto.report.application.dto.silk_car_record;

import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import org.bson.Document;

import java.util.Collection;

/**
 * @author jzb 2019-07-11
 */
public class SilkCarRecordAggregate_Runtime extends SilkCarRecordAggregate {

    protected SilkCarRecordAggregate_Runtime(Document document) {
        super(document);
    }

    @Override
    protected Grade fetchGrade() {
        return null;
    }

    @Override
    protected Collection<SilkRuntime> fetchInitSilkRuntimes() {
        return null;
    }

    @Override
    protected Batch fetchBatch() {
        return null;
    }
}
