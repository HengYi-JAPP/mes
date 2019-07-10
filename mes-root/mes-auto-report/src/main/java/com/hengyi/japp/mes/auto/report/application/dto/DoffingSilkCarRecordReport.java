package com.hengyi.japp.mes.auto.report.application.dto;

import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Grade;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.hengyi.japp.mes.auto.report.application.dto.silk_car_record.SilkCarRecordAggregate;
import lombok.Data;
import org.bson.Document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static java.util.stream.Collectors.*;

/**
 * @author jzb 2019-07-11
 */
@Data
public class DoffingSilkCarRecordReport implements Serializable {
    private final Collection<GroupBy_Batch_Grade> items;

    public DoffingSilkCarRecordReport(List<String> silkCarRecordIds) {
        items = silkCarRecordIds.parallelStream()
                .map(SilkCarRecordAggregate::from)
                .filter(it -> Objects.nonNull(it.getDoffingDateTime()))
                .collect(groupingBy(it -> it.getBatch().getString(ID_COL)))
                .entrySet().parallelStream()
                .flatMap(entry -> {
                    final Document batch = QueryService.findFromCache(Batch.class, entry.getKey()).get();
                    return entry.getValue().parallelStream()
                            .collect(groupingBy(it -> it.getGrade().getString(ID_COL)))
                            .entrySet().parallelStream()
                            .map(entry2 -> {
                                final Document grade = QueryService.findFromCache(Grade.class, entry2.getKey()).get();
                                return new GroupBy_Batch_Grade(batch, grade, entry2.getValue());
                            });
                })
                .collect(toList());
    }

    @Data
    public static class GroupBy_Batch_Grade {
        private final Document batch;
        private final Document grade;
        private final Collection<Item> items;
        private final int silkCount;
        private final BigDecimal netWeight;

        public GroupBy_Batch_Grade(Document batch, Document grade, Collection<SilkCarRecordAggregate> silkCarRecordAggregates) {
            this.batch = batch;
            this.grade = grade;
            items = silkCarRecordAggregates.parallelStream().map(it -> new Item(batch, grade, it)).collect(toList());
            silkCount = items.parallelStream().collect(summingInt(Item::getSilkCount));
            netWeight = items.parallelStream().map(Item::getNetWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    @Data
    public static class Item {
        private final Document batch;
        private final Document grade;
        private final SilkCarRecordAggregate silkCarRecordAggregate;
        private final int silkCount;
        private final BigDecimal netWeight;
        private final boolean hasNetWeight;

        public Item(Document batch, Document grade, SilkCarRecordAggregate silkCarRecordAggregate) {
            this.batch = batch;
            this.grade = grade;
            this.silkCarRecordAggregate = silkCarRecordAggregate;
            silkCount = silkCarRecordAggregate.getInitSilkRuntimes().size();
            if (grade.getInteger("sortBy") >= 100) {
                netWeight = BigDecimal.valueOf(batch.getDouble("silkWeight")).multiply(BigDecimal.valueOf(silkCount));
                hasNetWeight = true;
            } else {
                hasNetWeight = true;
//                silkCarRecordAggregate.getInitSilkRuntimes()
                netWeight = BigDecimal.valueOf(batch.getDouble("silkWeight")).multiply(BigDecimal.valueOf(silkCount));
            }
        }
    }

}
