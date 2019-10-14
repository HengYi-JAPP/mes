package com.hengyi.japp.mes.auto.report.application.dto.dyeing;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
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

import java.util.*;

import static com.hengyi.japp.mes.auto.report.Report.INJECTOR;
import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static com.mongodb.client.model.Filters.in;
import static java.util.stream.Collectors.toList;

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
        groupByOperators = Flux.from(dyeingPrepareCollection.find(in(ID_COL, dyeingPrepareIds)))
                .reduce(Maps.<String, GroupBy_Operator>newConcurrentMap(), (acc, cur) -> {
                    acc.compute(cur.getString("creator"), (k, v) -> Optional.ofNullable(v).orElse(new GroupBy_Operator(k)).collect(cur));
                    return acc;
                }).map(Map::values).block();
    }

    @SneakyThrows
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

    @Data
    public static class GroupBy_Operator {
        private final Operator operator = new Operator();
        private final Map<DyeingType, GroupBy_DyeingType> dyeingTypeMap = Maps.newConcurrentMap();

        public GroupBy_Operator(String id) {
            final Document operator = QueryService.find(Operator.class, id).block();
            this.operator.setId(operator.getString(ID_COL));
            this.operator.setName(operator.getString("name"));
            this.operator.setHrId(operator.getString("hrId"));
        }

        public GroupBy_Operator collect(Document dyeingPrepare) {
            final DyeingType dyeingType = DyeingType.valueOf(dyeingPrepare.getString("type"));
            dyeingTypeMap.compute(dyeingType, (k, v) -> Optional.ofNullable(v).orElse(new GroupBy_DyeingType(k)).collect(dyeingPrepare));
            return this;
        }
    }

    @Data
    public static class GroupBy_DyeingType {
        private final DyeingType dyeingType;
        private int silkCount = 0;

        public GroupBy_DyeingType(DyeingType dyeingType) {
            this.dyeingType = dyeingType;
        }

        private GroupBy_DyeingType collect(Document dyeingPrepare) {
            switch (dyeingType) {
                case CROSS_LINEMACHINE_LINEMACHINE: {
                    final List<String> silks1 = dyeingPrepare.getList("silks1", String.class);
                    final List<String> silks2 = dyeingPrepare.getList("silks2", String.class);
                    silkCount += J.emptyIfNull(silks1).size() + J.emptyIfNull(silks2).size();
                    return this;
                }
                default: {
                    final List<String> silks = dyeingPrepare.getList("silks", String.class);
                    silkCount += J.emptyIfNull(silks).size();
                    return this;
                }
            }
        }
    }
}
