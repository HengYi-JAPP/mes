package com.hengyi.japp.mes.auto.report.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import lombok.Data;
import lombok.Getter;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static java.util.stream.Collectors.*;

/**
 * @author jzb 2019-10-12
 */
public class PackageBoxReport {
    private final String workshopId;
    private final LocalDate startBudat;
    private final LocalDate endBudat;
    @Getter
    private final Collection<GroupBy_Operator> groupByOperators;

    public PackageBoxReport(String workshopId, LocalDate startBudat, LocalDate endBudat, Collection<String> packageBoxIds) {
        this.workshopId = workshopId;
        this.startBudat = startBudat;
        this.endBudat = endBudat;
        groupByOperators = Flux.fromIterable(J.emptyIfNull(packageBoxIds))
                .flatMap(it -> QueryService.find(PackageBox.class, it)).toStream()
                .collect(groupingBy(it -> it.getString("creator")))
                .entrySet().stream()
                .map(entry -> {
                    final String operatorId = entry.getKey();
                    final Document operator = QueryService.find(Operator.class, operatorId).block();
                    return new GroupBy_Operator(operator, entry.getValue());
                })
                .collect(toList());
    }

    public static PackageBoxReport create(String workshopId, LocalDate startBudat, LocalDate endBudat) {
        final QueryService queryService = INJECTOR.getInstance(QueryService.class);
        final Collection<String> ids = queryService.queryPackageBoxIds(workshopId, startBudat, endBudat);
        return new PackageBoxReport(workshopId, startBudat, endBudat, ids);
    }

    public static PackageBoxReport create(String workshopId, long startDateTime, long endDateTime) {
        final QueryService queryService = INJECTOR.getInstance(QueryService.class);
        final Collection<String> ids = queryService.queryPackageBoxIds(workshopId, startDateTime, endDateTime);
        return new PackageBoxReport(workshopId, null, null, ids);
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
        private final int packageBoxCount;
        private final int silkCountSum;
        private final BigDecimal netWeightSum;

        public GroupBy_Operator(Document operator, Collection<Document> packageBoxes) {
            this.operator.setId(operator.getString(ID_COL));
            this.operator.setName(operator.getString("name"));
            this.operator.setHrId(operator.getString("hrId"));
            packageBoxCount = packageBoxes.size();
            silkCountSum = packageBoxes.parallelStream().collect(summingInt(it -> it.getInteger("silkCount")));
            netWeightSum = packageBoxes.parallelStream().map(it -> {
                final Double netWeight = it.getDouble("netWeight");
                return netWeight == null ? BigDecimal.ZERO : BigDecimal.valueOf(netWeight);
            }).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public JsonNode toJsonNode() {
            final Map<String, Object> map = Map.of("operator", this.operator, "packageBoxCount", packageBoxCount, "silkCountSum", silkCountSum, "netWeightSum", netWeightSum);
            return MAPPER.convertValue(map, JsonNode.class);
        }
    }
}
