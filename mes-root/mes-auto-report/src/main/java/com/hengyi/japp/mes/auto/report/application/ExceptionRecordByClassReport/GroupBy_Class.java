package com.hengyi.japp.mes.auto.report.application.ExceptionRecordByClassReport;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;

/**
 * @author jzb 2019-12-16
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GroupBy_Class {
    @EqualsAndHashCode.Include
    private final String classCode;
    private int silkCount = 0;

    GroupBy_Class(String classCode) {
        this.classCode = classCode;
    }

    public GroupBy_Class collect(Document exceptionRecord) {
        silkCount++;
        return this;
    }
}
