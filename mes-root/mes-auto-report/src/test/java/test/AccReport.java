package test;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.application.event.ProductProcessSubmitEvent;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.hengyi.japp.mes.auto.report.application.dto.silk_car_record.SilkCarRecordAggregate;
import lombok.Data;
import lombok.SneakyThrows;
import org.bson.Document;

import java.io.File;
import java.util.*;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-10-12
 */
public class AccReport {
    private static final Collection<String> PRODUCT_PROCESS_IDS = Set.of("5bffac38e189c40001863e47", "5c0084d951e9c40001573d43");
    private final String workshopId;
    private final long startDateTime;
    private final long endDateTime;
    private final Map<String, GroupBy_Operator> InspectionMap = Maps.newConcurrentMap();
    private final Map<String, GroupBy_Operator> DoffingMap = Maps.newConcurrentMap();

    public AccReport(String workshopId, long startDateTime, long endDateTime) {
        this.workshopId = workshopId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public AccReport collect(SilkCarRecordAggregate silkCarRecordAggregate) {
        final String inspectionOperatorId = InspectionOperatorId(silkCarRecordAggregate);
        if (inspectionOperatorId != null) {
            InspectionMap.compute(inspectionOperatorId, (k, v) -> {
                if (v == null) {
                    final Document operator = QueryService.find(Operator.class, inspectionOperatorId).block();
                    v = new GroupBy_Operator(operator);
                }
                return v.collect(silkCarRecordAggregate);
            });
        }
        final String doffingOperatorId = silkCarRecordAggregate.getDoffingOperatorId();
        if (doffingOperatorId != null) {
            DoffingMap.compute(doffingOperatorId, (k, v) -> {
                if (v == null) {
                    final Document operator = QueryService.find(Operator.class, doffingOperatorId).block();
                    v = new GroupBy_Operator(operator);
                }
                return v.collect(silkCarRecordAggregate);
            });
        }
        return this;
    }

    @SneakyThrows
    public void save(String workshopId) {
        MAPPER.writeValue(new File("/home/jzb/test/InspectionReport/" + workshopId + ".json"), InspectionMap.values());
        MAPPER.writeValue(new File("/home/jzb/test/DoffingReport/" + workshopId + ".json"), DoffingMap.values());
    }

    private String InspectionOperatorId(SilkCarRecordAggregate silkCarRecordAggregate) {
        final Document batch = silkCarRecordAggregate.getBatch();
        if (!Objects.equals(workshopId, batch.getString("workshop"))) {
            return null;
        }
        final long time = silkCarRecordAggregate.getStartDateTime().getTime();
        if (time >= endDateTime) {
            return null;
        }
        final Optional<EventSource.DTO> optional = findEventSourceDTO(silkCarRecordAggregate.getEventSourceDtos());
        if (optional.isPresent()) {
            final EventSource.DTO dto = optional.get();
            final long fireL = dto.getFireDateTime().getTime();
            if (fireL >= this.startDateTime && fireL < endDateTime) {
                return dto.getOperator().getId();
            }
        }
        return null;
    }

    private Optional<EventSource.DTO> findEventSourceDTO(Collection<EventSource.DTO> eventSourceDtos) {
        final List<EventSource.DTO> dtos = eventSourceDtos.parallelStream().filter(it -> {
            if (!it.isDeleted() && EventSourceType.ProductProcessSubmitEvent == it.getType()) {
                final ProductProcessSubmitEvent.DTO dto = (ProductProcessSubmitEvent.DTO) it;
                return PRODUCT_PROCESS_IDS.contains(dto.getProductProcess().getId());
            }
            return false;
        }).collect(toList());
        if (J.isEmpty(dtos)) {
            return Optional.empty();
        }
        Collections.sort(dtos);
        return Optional.of(dtos.get(0));
    }

    @Data
    public static class GroupBy_Operator {
        private final Operator operator = new Operator();
        private int silkCarRecordCount;
        private int silkCount;

        public GroupBy_Operator(Document operator) {
            this.operator.setId(operator.getString(ID_COL));
            this.operator.setName(operator.getString("name"));
            this.operator.setHrId(operator.getString("hrId"));
        }

        public GroupBy_Operator collect(SilkCarRecordAggregate silkCarRecordAggregate) {
            silkCarRecordCount++;
            silkCount += silkCarRecordAggregate.getInitSilkRuntimeDtos().size();
            return this;
        }
    }
}
