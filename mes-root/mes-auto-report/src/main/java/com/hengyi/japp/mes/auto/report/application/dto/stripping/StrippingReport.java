package com.hengyi.japp.mes.auto.report.application.dto.stripping;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.application.event.ProductProcessSubmitEvent;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.Product;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.hengyi.japp.mes.auto.report.application.RedisService;
import com.hengyi.japp.mes.auto.report.application.dto.silk_car_record.SilkCarRecordAggregate;
import lombok.Data;
import lombok.Getter;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.util.*;

import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-09-04
 */
public class StrippingReport {
    private static final Collection<String> PRODUCT_PROCESS_IDS = Set.of("5bffac20e189c40001863d76", "5bffad09e189c40001864331");
    private final String workshopId;
    private final long startDateTime;
    private final long endDateTime;
    @Getter
    private final Collection<GroupBy_Operator> groupByOperators;

    public StrippingReport(String workshopId, long startDateTime, long endDateTime, Collection<String> silkCarRecordIds) {
        this.workshopId = workshopId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        groupByOperators = Flux.fromIterable(J.emptyIfNull(silkCarRecordIds))
                .flatMap(SilkCarRecordAggregate::from)
                .filter(it -> Objects.equals(DoffingType.AUTO, it.getDoffingType()) || Objects.equals(DoffingType.MANUAL, it.getDoffingType()))
                .reduce(Maps.<String, GroupBy_Operator>newConcurrentMap(), (acc, cur) -> operatorId(cur).map(operatorId -> {
                    acc.compute(operatorId, (k, v) -> Optional.ofNullable(v).orElse(new GroupBy_Operator(k)).collect(cur));
                    return acc;
                }).orElse(acc)).map(Map::values).block();
    }

    public static StrippingReport create(String workshopId, long startDateTime, long endDateTime) {
        final QueryService queryService = INJECTOR.getInstance(QueryService.class);
        final Collection<String> ids = RedisService.listSilkCarRuntimeSilkCarRecordIds();
        ids.addAll(queryService.querySilkCarRecordIdsByEventSourceCanHappen(workshopId, startDateTime, endDateTime));
        return new StrippingReport(workshopId, startDateTime, endDateTime, ids);
    }

    /**
     * 当前所有丝车，会包含所有车间，所有时间，需要特别过滤
     *
     * @param silkCarRecordAggregate
     * @return
     */
    private Optional<String> operatorId(SilkCarRecordAggregate silkCarRecordAggregate) {
        final Document batch = silkCarRecordAggregate.getBatch();
        if (!Objects.equals(workshopId, batch.getString("workshop"))) {
            return Optional.empty();
        }
        final long time = silkCarRecordAggregate.getStartDateTime().getTime();
        if (time >= endDateTime) {
            return Optional.empty();
        }
        return findEventSourceDTO(silkCarRecordAggregate.getEventSourceDtos())
                .map(EventSource.DTO::getOperator)
                .map(EntityDTO::getId);
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
        return Optional.of(dtos.get(0)).filter(dto -> {
            final long fireL = dto.getFireDateTime().getTime();
            return fireL >= this.startDateTime && fireL < endDateTime;
        });
    }

    @Data
    public static class GroupBy_Operator {
        private final Operator operator = new Operator();
        private final Map<String, GroupBy_Product> productMap = Maps.newConcurrentMap();

        public GroupBy_Operator(String id) {
            final Document operator = QueryService.find(Operator.class, id).block();
            this.operator.setId(operator.getString(ID_COL));
            this.operator.setName(operator.getString("name"));
            this.operator.setHrId(operator.getString("hrId"));
        }

        public GroupBy_Operator collect(SilkCarRecordAggregate silkCarRecordAggregate) {
            final Document batch = silkCarRecordAggregate.getBatch();
            productMap.compute(batch.getString("product"), (k, v) -> Optional.ofNullable(v).orElse(new GroupBy_Product(k)).collect(silkCarRecordAggregate));
            return this;
        }
    }

    @Data
    public static class GroupBy_Product {
        private final Product product = new Product();
        private int silkCarRecordCount;
        private int silkCount;

        public GroupBy_Product(String id) {
            final Document product = QueryService.find(Product.class, id).block();
            this.product.setId(product.getString(ID_COL));
            this.product.setName(product.getString("name"));
        }

        public GroupBy_Product collect(SilkCarRecordAggregate silkCarRecordAggregate) {
            silkCarRecordCount++;
            silkCount += silkCarRecordAggregate.getInitSilkRuntimeDtos().size();
            return this;
        }
    }

}
