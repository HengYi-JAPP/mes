package com.hengyi.japp.mes.auto.report.application.SilkExceptionReport;

import com.hengyi.japp.mes.auto.domain.SilkException;
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
public class GroupBy_SilkException {
    @EqualsAndHashCode.Include
    private final SilkException silkException = new SilkException();
    private int silkCount = 0;

    GroupBy_SilkException(String exceptionId) {
        final Document silkException = findFromCache(SilkException.class, exceptionId).get();
        this.silkException.setId(silkException.getString(ID_COL));
        this.silkException.setName(silkException.getString("name"));
    }

    GroupBy_SilkException collect(Document exceptionRecord) {
        silkCount++;
        return this;
    }
}
