package com.hengyi.japp.mes.auto.report.application.SilkExceptionReport;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.hengyi.japp.mes.auto.report.application.dto.silk_car_record.SilkCarRecordAggregate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;

import java.util.Map;

import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static java.util.Optional.ofNullable;

/**
 * @author jzb 2019-12-16
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GroupBy_Line {
    @EqualsAndHashCode.Include
    private final Line line = new Line();
    private final Map<String, GroupBy_Batch> batchMap = Maps.newConcurrentMap();

    public GroupBy_Line(Document line) {
        this.line.setId(line.getString(ID_COL));
        this.line.setName(line.getString("name"));
    }

    GroupBy_Line collect(Document exceptionRecord) {
        final String silkId = exceptionRecord.getString("silk");
        if (J.nonBlank(silkId)) {
            final String batchId = QueryService.find(Silk.class, silkId).map(it -> it.getString("batch")).block();
            batchMap.compute(batchId, (k, v) -> ofNullable(v).orElse(new GroupBy_Batch(k)).collect(exceptionRecord));
        }
        return this;
    }

    GroupBy_Line collect(SilkCarRecordAggregate silkCarRecordAggregate, Document silk) {
        final Document batch = silkCarRecordAggregate.getBatch();
        final String batchId = batch.getString(ID_COL);
        batchMap.compute(batchId, (k, v) -> ofNullable(v).orElse(new GroupBy_Batch(k)).collect(silkCarRecordAggregate, silk));
        return this;
    }
}
