package com.hengyi.japp.mes.auto.report.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.hengyi.japp.mes.auto.report.application.dto.silk_car_record.SilkCarRecordAggregate;
import lombok.Data;
import lombok.SneakyThrows;
import org.bson.Document;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-07-11
 */
@Data
public class DoffingSilkCarRecordReportByDoffingOperator implements Serializable {
    private final Collection<Item> items;

    public DoffingSilkCarRecordReportByDoffingOperator(List<String> silkCarRecordIds) {
        items = silkCarRecordIds.parallelStream()
                .map(SilkCarRecordAggregate::from)
                .filter(it -> Objects.nonNull(it.getDoffingDateTime()))
                .collect(Collectors.groupingBy(SilkCarRecordAggregate::getDoffingOperatorId))
                .entrySet().parallelStream()
                .map(entry -> {
                    final Document operator = QueryService.findFromCache(Operator.class, entry.getKey()).get();
                    return new Item(operator, entry.getValue());
                })
                .collect(toList());
    }

    public JsonNode toJsonNode() {
        final ArrayNode result = MAPPER.createArrayNode();
        items.stream().map(Item::toJsonNode).forEach(result::add);
        return result;
    }

    @Data
    public static class Item {
        private final Document operator;
        private final Collection<SilkCarRecordAggregate> silkCarRecordAggregates;

        @SneakyThrows
        public JsonNode toJsonNode() {
            final ObjectNode result = MAPPER.createObjectNode()
                    .put("count", silkCarRecordAggregates.size());
            final ObjectNode operatorNode = MAPPER.createObjectNode()
                    .put("id", operator.getString(ID_COL))
                    .put("hrId", operator.getString("hrId"))
                    .put("name", operator.getString("name"));
            result.set("operator", operatorNode);
            return result;
        }
    }
}
