package com.hengyi.japp.mes.auto.report.application.SilkExceptionReport;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeGradeSubmitEvent;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.Product;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.report.application.dto.silk_car_record.SilkCarRecordAggregate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static com.hengyi.japp.mes.auto.report.application.QueryService.findFromCache;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-12-16
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GroupBy_Batch {
    @EqualsAndHashCode.Include
    private final Batch batch = new Batch();
    private final Map<String, GroupBy_SilkException> silkExceptionMap = Maps.newConcurrentMap();
    private final Map<String, GroupBy_Grade> gradeMap = Maps.newConcurrentMap();
    private final GroupBy_NoGrade noGrade = new GroupBy_NoGrade("5d35d5fe87b4a50001736dca");
    private int silkCount = 0;

    GroupBy_Batch(String batchId) {
        final Document batch = findFromCache(Batch.class, batchId).get();
        this.batch.setId(batch.getString(ID_COL));
        this.batch.setBatchNo(batch.getString("batchNo"));
        this.batch.setSpec(batch.getString("spec"));
        this.batch.setSilkWeight(batch.getDouble("silkWeight"));
        final Product product = new Product();
        this.batch.setProduct(product);
        final Document document = findFromCache(Product.class, batch.getString("product")).get();
        product.setId(document.getString(ID_COL));
        product.setName(document.getString("name"));
    }

    GroupBy_Batch collect(Document exceptionRecord) {
        final String exceptionId = exceptionRecord.getString("exception");
        silkExceptionMap.compute(exceptionId, (k, v) -> ofNullable(v).orElse(new GroupBy_SilkException(k)).collect(exceptionRecord));
        return this;
    }

    GroupBy_Batch collect(SilkCarRecordAggregate silkCarRecordAggregate, Document silk) {
        silkCount++;
        final Document grade = silkCarRecordAggregate.getGrade();
        if (grade.getInteger("sortBy") < 60) {
            final List<SilkCarRuntimeGradeSubmitEvent.DTO> gradeSubmitEvents = silkCarRecordAggregate.getEventSourceDtos().stream()
                    .filter(it -> !it.isDeleted() && EventSourceType.SilkCarRuntimeGradeSubmitEvent == it.getType())
                    .map(SilkCarRuntimeGradeSubmitEvent.DTO.class::cast)
                    .sorted().collect(toList());
            if (J.nonEmpty(gradeSubmitEvents)) {
                final SilkCarRuntimeGradeSubmitEvent.DTO dto = gradeSubmitEvents.get(gradeSubmitEvents.size() - 1);
                final SilkCarRuntimeGradeSubmitEvent.ItemDTO itemDTO = J.emptyIfNull(dto.getSilkRuntimes()).stream().filter(it -> {
                    @NotBlank final String silkId = ofNullable(it).map(SilkRuntime.DTO::getSilk).map(EntityDTO::getId).orElse(null);
                    return Objects.equals(silk.getString(ID_COL), silkId);
                }).findFirst().orElse(null);
                if (itemDTO != null) {
                    @NotNull final String submitGradeId = itemDTO.getGrade().getId();
                    gradeMap.compute(submitGradeId, (k, v) -> ofNullable(v).orElse(new GroupBy_Grade(submitGradeId)).collect(silkCarRecordAggregate, silk));
                }
            } else {
                noGrade.collect(silkCarRecordAggregate, silk);
            }
        }
        return this;
    }
}
