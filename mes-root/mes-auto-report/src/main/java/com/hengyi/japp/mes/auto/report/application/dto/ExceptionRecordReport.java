package com.hengyi.japp.mes.auto.report.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.domain.ExceptionRecord;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.report.Report;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.Data;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static com.mongodb.client.model.Filters.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-10-12
 */
public class ExceptionRecordReport {
    private final String workshopId;
    private final long startDateTime;
    private final long endDateTime;
    @Getter
    private final Collection<GroupBy_Operator> groupByOperators;

    public ExceptionRecordReport(String workshopId, long startDateTime, long endDateTime, Flux<Document> exceptionRecord$) {
        this.workshopId = workshopId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        groupByOperators = exceptionRecord$.toStream()
                .collect(groupingBy(it -> it.getString("creator")))
                .entrySet().stream()
                .map(entry -> {
                    final String operatorId = entry.getKey();
                    final Document operator = QueryService.find(Operator.class, operatorId).block();
                    return new GroupBy_Operator(operator, entry.getValue());
                })
                .collect(toList());
    }

    public static ExceptionRecordReport create(String workshopId, long startDateTime, long endDateTime) {
        final MongoCollection<Document> T_ExceptionRecord = Report.mongoCollection(ExceptionRecord.class);
        final Bson startFilter = gte("cdt", new Date(startDateTime));
        final Bson endFilter = lte("cdt", new Date(endDateTime));
        final Flux<Document> exceptionRecord$ = Flux.from(T_ExceptionRecord.find(and(startFilter, endFilter)));
        return new ExceptionRecordReport(workshopId, startDateTime, endDateTime, exceptionRecord$);
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
        private final int silkCount;

        public GroupBy_Operator(Document operator, Collection<Document> exceptionRecords) {
            this.operator.setId(operator.getString(ID_COL));
            this.operator.setName(operator.getString("name"));
            this.operator.setHrId(operator.getString("hrId"));
            silkCount = exceptionRecords.size();
        }

        public JsonNode toJsonNode() {
            final Map<String, Object> map = Map.of("operator", this.operator, "silkCount", silkCount);
            return MAPPER.convertValue(map, JsonNode.class);
        }
    }
}
