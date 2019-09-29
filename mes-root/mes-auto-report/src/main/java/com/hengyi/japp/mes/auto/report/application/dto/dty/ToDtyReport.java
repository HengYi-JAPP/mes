package com.hengyi.japp.mes.auto.report.application.dto.dty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.application.event.EventSource;
import com.hengyi.japp.mes.auto.application.event.EventSourceType;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.hengyi.japp.mes.auto.report.application.RedisService;
import com.hengyi.japp.mes.auto.report.application.dto.silk_car_record.SilkCarRecordAggregate;
import lombok.Data;
import lombok.Getter;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.util.*;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static java.util.stream.Collectors.*;

/**
 * @author jzb 2019-09-26
 */
public class ToDtyReport {
    private static final String ANONYMOUS = "anonymous";
    private static final String BLANK = "";
    private final String workshopId;
    private final long startDateTime;
    private final long endDateTime;
    @Getter
    private final Collection<GroupBy_Operator> groupByOperators;

    public ToDtyReport(String workshopId, long startDateTime, long endDateTime, Collection<String> silkCarRecordIds) {
        this.workshopId = workshopId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        groupByOperators = Flux.fromIterable(J.emptyIfNull(silkCarRecordIds))
                .flatMap(SilkCarRecordAggregate::from).toStream()
                .collect(groupingBy(this::operatorId))
                .entrySet().stream()
                .filter(entry -> !Objects.equals(BLANK, entry.getKey()))
                .map(entry -> {
                    final String operatorId = entry.getKey();
                    final Document operator;
                    if (ANONYMOUS.equals(operatorId)) {
                        operator = null;
                    } else {
                        operator = QueryService.find(Operator.class, operatorId).block();
                    }
                    return new GroupBy_Operator(operator, entry.getValue());
                })
                .collect(toList());
    }

    public static ToDtyReport create(String workshopId, long startDateTime, long endDateTime) {
        final QueryService queryService = INJECTOR.getInstance(QueryService.class);
        final Collection<String> ids = RedisService.listSilkCarRuntimeSilkCarRecordIds();
        ids.addAll(queryService.querySilkCarRecordIdsByEventSourceCanHappen(workshopId, startDateTime, endDateTime));
        return new ToDtyReport(workshopId, startDateTime, endDateTime, ids);
    }

    /**
     * 当前所有丝车，会包含所有车间，所有时间，需要特别过滤
     *
     * @param silkCarRecordAggregate
     * @return
     */
    private String operatorId(SilkCarRecordAggregate silkCarRecordAggregate) {
        final Document batch = silkCarRecordAggregate.getBatch();
        if (!Objects.equals(workshopId, batch.getString("workshop"))) {
            return BLANK;
        }
        final long time = silkCarRecordAggregate.getStartDateTime().getTime();
        if (time >= endDateTime) {
            return BLANK;
        }
        final Optional<EventSource.DTO> optional = findEventSourceDTO(silkCarRecordAggregate.getEventSourceDtos());
        if (optional.isPresent()) {
            final EventSource.DTO dto = optional.get();
            final long fireL = dto.getFireDateTime().getTime();
            if (fireL >= this.startDateTime && fireL < endDateTime) {
                return dto.getOperator().getId();
            } else {
                return BLANK;
            }
        } else {
            if (silkCarRecordAggregate.getEndDateTime() == null) {
                return BLANK;
            } else {
                return ANONYMOUS;
            }
        }
    }

    private Optional<EventSource.DTO> findEventSourceDTO(Collection<EventSource.DTO> eventSourceDtos) {
        final List<EventSource.DTO> dtos = eventSourceDtos.parallelStream()
                .filter(it -> !it.isDeleted() && EventSourceType.ToDtyEvent == it.getType())
                .collect(toList());
        if (J.isEmpty(dtos)) {
            return Optional.empty();
        }
        Collections.sort(dtos);
        return Optional.of(dtos.get(0));
    }

    public JsonNode toJsonNode() {
        final ArrayNode arrayNode = MAPPER.createArrayNode();
        J.emptyIfNull(groupByOperators).stream()
                .map(GroupBy_Operator::toJsonNode)
                .forEach(arrayNode::add);
        return arrayNode;
    }

    @Data
    public static class GroupBy_Operator {
        private final Operator operator = new Operator();
        private final Collection<SilkCarRecordAggregate> silkCarRecordAggregates;
        private final int silkCarRecordCount;
        private final int silkCount;

        public GroupBy_Operator(Document operator, Collection<SilkCarRecordAggregate> silkCarRecordAggregates) {
            if (operator == null) {
                this.operator.setId(ANONYMOUS);
                this.operator.setName("漏扫");
            } else {
                this.operator.setId(operator.getString(ID_COL));
                this.operator.setName(operator.getString("name"));
                this.operator.setHrId(operator.getString("hrId"));
            }
            this.silkCarRecordAggregates = silkCarRecordAggregates;
            silkCarRecordCount = silkCarRecordAggregates.size();
            silkCount = this.silkCarRecordAggregates.parallelStream().collect(summingInt(it -> it.getInitSilkRuntimeDtos().size()));
        }

        public JsonNode toJsonNode() {
            final Map<String, Object> map = Map.of("operator", this.operator, "silkCarRecordCount", silkCarRecordCount, "silkCount", silkCount);
            return MAPPER.convertValue(map, JsonNode.class);
        }
    }
}