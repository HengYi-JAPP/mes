package com.hengyi.japp.mes.auto.report.application;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.hengyi.japp.mes.auto.domain.DyeingPrepare;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.report.Jlucene;
import com.hengyi.japp.mes.auto.report.application.command.DyeingReportCommand;
import io.reactivex.Single;
import io.vertx.reactivex.core.eventbus.Message;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * @author liuyuan
 * @create 2019-05-29 18:16
 * @description
 **/
public class DyeingReportService {

    private final QueryService queryService;

    @Inject
    public DyeingReportService(QueryService queryService) {
        this.queryService = queryService;
    }

    @SneakyThrows
    public void dyeingReport(Message<Object> reply) {
        DyeingReportCommand command = DyeingReportCommand.dyeingReportCommand(reply.body());
        BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final long startL = J.date(LocalDateTime.parse(command.getStartDateTime(), df)).getTime();
        final long endL = J.date(LocalDateTime.parse(command.getEndDateTime(), df)).getTime();
        bqBuilder
                .add(LongPoint.newRangeQuery("createDateTime", startL, endL), BooleanClause.Occur.MUST);
        Jlucene.add(bqBuilder, "workshop", command.getWorkshopId());
        @Cleanup final IndexReader indexReader = queryService.indexReader(DyeingPrepare.class);
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(bqBuilder.build(), Integer.MAX_VALUE);
        final Stream<String> idStream = Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> Jlucene.toDocument(searcher, scoreDoc))
                .map(it -> it.get("id"));
        List<Document> dyeingPrepares = Flux.fromStream(idStream)
                .flatMap(id -> QueryService.find(DyeingPrepare.class, id))
                .map(document -> {
                    Document creator = QueryService.find(Operator.class, document.getString("creator"))
                            .block();
                    return document.append("creator", creator);
                })
                .collectList()
                .block();
        Single.just(dyeingPrepares)
                .map(list -> {
                    Map<Document, List<Document>> groupMap = list.parallelStream().collect(Collectors.groupingBy(document -> (Document) document.get("creator"), Collectors.toList()));
                    return groupMap.keySet().stream().map(key -> {
                        Document doc = key;
                        int silkCount = J.emptyIfNull(groupMap.get(key)).parallelStream()
                                .map(document -> {
                                    int count;
                                    if ("FIRST".equals(document.getString("type"))) {
                                        int size = ((Collection) document.get("silks")).size();
                                        if (size == 12) {
                                            count = size + 1;
                                        } else {
                                            count = size + 2;
                                        }
                                    } else {
                                        if (document.get("silks") != null) {
                                            count = ((Collection) document.get("silks")).size() * 2;
                                        } else {
                                            int count1 = ((Collection) document.get("silks1")).size() * 2;
                                            int count2 = ((Collection) document.get("silks2")).size() * 2;
                                            count = count1 + count2;
                                        }
                                    }
                                    return count;
                                }).reduce((a, b) -> a + b).get();
                        int silkCarCount = J.emptyIfNull(groupMap.get(key)).parallelStream().collect(collectingAndThen(
                                toCollection(() -> new TreeSet<>(Comparator.comparing(document -> {
                                    if (document.getString("silkCarRecord") != null) {
                                        return document.getString("silkCarRecord");
                                    }
                                    return document.getString("silkCarRecord1");
                                }))), ArrayList::new)).size();
                        doc.append("operatorName", key.getString("name"));
                        doc.append("silkCount", silkCount);
                        doc.append("silkCarCount", silkCarCount);
                        return doc;
                    }).toArray();
                })
                .map(MAPPER::writeValueAsString)
                .subscribe(reply::reply, err -> reply.fail(400, err.getMessage()));
    }
}
