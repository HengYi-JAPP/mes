package com.hengyi.japp.mes.auto.report.application.dto.dyeing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.domain.DyeingPrepare;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.data.DyeingType;
import com.hengyi.japp.mes.auto.report.Jlucene;
import com.hengyi.japp.mes.auto.report.Report;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.Cleanup;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static com.mongodb.client.model.Filters.in;
import static java.util.stream.Collectors.*;

/**
 * @author jzb 2019-09-26
 */
public class DyeingReport {
    private final String workshopId;
    private final long startDateTime;
    private final long endDateTime;
    @Getter
    private final Collection<GroupBy_Operator> groupByOperators;

    public DyeingReport(String workshopId, long startDateTime, long endDateTime, Collection<String> dyeingPrepareIds) {
        this.workshopId = workshopId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        final MongoCollection<Document> dyeingPrepareCollection = Report.mongoCollection(DyeingPrepare.class);
        groupByOperators = Flux.from(dyeingPrepareCollection.find(in(ID_COL, dyeingPrepareIds))).toStream()
                .collect(groupingBy(it -> it.getString("creator")))
                .entrySet().stream()
                .map(entry -> {
                    final String operatorId = entry.getKey();
                    final Document operator = QueryService.find(Operator.class, operatorId).block();
                    return new GroupBy_Operator(operator, entry.getValue());
                })
                .collect(toList());
    }

    @SneakyThrows(IOException.class)
    public static DyeingReport create(String workshopId, long startDateTime, long endDateTime) {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        Jlucene.add(bqBuilder, "workshop", workshopId);
        Jlucene.addRangeQuery(bqBuilder, "createDateTime", startDateTime, endDateTime);

        final QueryService queryService = INJECTOR.getInstance(QueryService.class);
        @Cleanup final IndexReader indexReader = queryService.indexReader(DyeingPrepare.class);
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(bqBuilder.build(), Integer.MAX_VALUE);
        final List<String> ids = Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> Jlucene.toDocument(searcher, scoreDoc))
                .map(it -> it.get("id"))
                .collect(toList());
        return new DyeingReport(workshopId, startDateTime, endDateTime, ids);
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
        private final Collection<GroupBy_DyeingType> groupByDyeingTypes;

        public GroupBy_Operator(Document operator, Collection<Document> dyeingPrepares) {
            this.operator.setId(operator.getString(ID_COL));
            this.operator.setName(operator.getString("name"));
            this.operator.setHrId(operator.getString("hrId"));
            groupByDyeingTypes = dyeingPrepares.stream()
                    .collect(groupingBy(it -> it.getString("type")))
                    .entrySet().stream()
                    .map(entry -> {
                        final DyeingType dyeingType = DyeingType.valueOf(entry.getKey());
                        return new GroupBy_DyeingType(dyeingType, entry.getValue());
                    })
                    .collect(toList());
        }

        public JsonNode toJsonNode() {
            final ArrayNode groupByDyeingTypesNode = MAPPER.createArrayNode();
            J.emptyIfNull(groupByDyeingTypes).stream().map(GroupBy_DyeingType::toJsonNode).forEach(groupByDyeingTypesNode::add);
            final Map<String, Object> map = Map.of("operator", this.operator, "groupByDyeingTypes", groupByDyeingTypesNode);
            return MAPPER.convertValue(map, JsonNode.class);
        }
    }

    @Data
    public static class GroupBy_DyeingType {
        private final DyeingType dyeingType;
        private final Collection<Document> dyeingPrepares;
        private final int silkCount;

        public GroupBy_DyeingType(DyeingType dyeingType, Collection<Document> dyeingPrepares) {
            this.dyeingType = dyeingType;
            this.dyeingPrepares = dyeingPrepares;
            silkCount = this.dyeingPrepares.parallelStream().collect(summingInt(it -> {
                final List<String> list = it.getList("dyeingResults", String.class);
                return J.emptyIfNull(list).size();
            }));
        }

        public JsonNode toJsonNode() {
            final Map<String, Object> map = Map.of("dyeingType", dyeingType, "silkCount", silkCount);
            return MAPPER.convertValue(map, JsonNode.class);
        }
    }
}
