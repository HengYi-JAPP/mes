package com.hengyi.japp.mes.auto.report.application.ExceptionRecordByClassReport;

import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.domain.SilkException;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;

import java.util.Map;

import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static com.hengyi.japp.mes.auto.report.application.QueryService.findFromCache;
import static java.util.Optional.ofNullable;

/**
 * @author jzb 2019-12-16
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GroupBy_SilkException {
    @EqualsAndHashCode.Include
    private final SilkException silkException = new SilkException();
    private final Map<String, GroupBy_Class> classMap = Maps.newConcurrentMap();
    private int silkCount = 0;

    GroupBy_SilkException(String exceptionId) {
        final Document silkException = findFromCache(SilkException.class, exceptionId).get();
        this.silkException.setId(silkException.getString(ID_COL));
        this.silkException.setName(silkException.getString("name"));
    }

    GroupBy_SilkException collect(Document exceptionRecord) {
        silkCount++;
        final String silkId = exceptionRecord.getString("silk");
        QueryService.find(Silk.class, silkId).blockOptional().ifPresent(silk -> {
            final String doffingNum = silk.getString("doffingNum");
            final String classCode = doffingNum.substring(0, 1);
            classMap.compute(classCode, (k, v) -> ofNullable(v).orElse(new GroupBy_Class(k)).collect(exceptionRecord));
        });
        return this;
    }
}
