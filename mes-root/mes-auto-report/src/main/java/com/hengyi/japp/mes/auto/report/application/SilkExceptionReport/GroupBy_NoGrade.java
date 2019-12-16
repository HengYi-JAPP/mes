package com.hengyi.japp.mes.auto.report.application.SilkExceptionReport;

import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.report.application.dto.silk_car_record.SilkCarRecordAggregate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;

import java.util.Map;

/**
 * @author jzb 2019-12-16
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class GroupBy_NoGrade extends GroupBy_Grade {
    private final Map<String, SilkCarRecordAggregate> silkCarRecordMap = Maps.newConcurrentMap();

    GroupBy_NoGrade(String gradeId) {
        super(gradeId);
    }

    GroupBy_NoGrade collect(SilkCarRecordAggregate silkCarRecordAggregate, Document silk) {
        super.collect(silkCarRecordAggregate, silk);
        silkCarRecordMap.putIfAbsent(silkCarRecordAggregate.getId(), silkCarRecordAggregate);
        return this;
    }
}
