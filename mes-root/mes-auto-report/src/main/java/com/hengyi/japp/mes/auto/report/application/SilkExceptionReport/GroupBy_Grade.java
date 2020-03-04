package com.hengyi.japp.mes.auto.report.application.SilkExceptionReport;

import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.report.application.dto.silk_car_record.SilkCarRecordAggregate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;

import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static com.hengyi.japp.mes.auto.report.application.QueryService.findFromCache;

/**
 * @author jzb 2019-12-16
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GroupBy_Grade {
    @EqualsAndHashCode.Include
    private final Grade grade = new Grade();
    private int silkCount = 0;

    GroupBy_Grade(String gradeId) {
        final Document grade = findFromCache(Grade.class, gradeId).get();
        this.grade.setId(grade.getString(ID_COL));
        this.grade.setName(grade.getString("name"));
        this.grade.setCode(grade.getString("code"));
        this.grade.setSortBy(grade.getInteger("sortBy"));
    }

    GroupBy_Grade collect(SilkCarRecordAggregate silkCarRecordAggregate, Document silk) {
        silkCount++;
        return this;
    }
}
