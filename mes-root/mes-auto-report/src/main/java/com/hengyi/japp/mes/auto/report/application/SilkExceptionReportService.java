package com.hengyi.japp.mes.auto.report.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.report.Jlucene;
import com.hengyi.japp.mes.auto.report.RedisUtil;
import com.hengyi.japp.mes.auto.report.application.command.SilkExceptionReportCommand;
import com.hengyi.japp.mes.auto.report.application.dto.silkException.SilkExceptionReport;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.core.eventbus.Message;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author liuyuan
 * @create 2019-05-29 18:16
 * @description
 **/
@Slf4j
@Singleton
public class SilkExceptionReportService {
    private final QueryService queryService;

    @Inject
    public SilkExceptionReportService(QueryService queryService) {
        this.queryService = queryService;
    }

    @SneakyThrows
    public void silkExceptionReport(Message<Object> reply) {
        SilkExceptionReportCommand command = SilkExceptionReportCommand.silkExceptionReportCommand(reply.body());
        BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        Jlucene.add(bqBuilder, "workshop", command.getWorkshopId());
        bqBuilder.add(LongPoint.newRangeQuery("endDateTime", command.getStartDateTime(), Long.MAX_VALUE), BooleanClause.Occur.MUST);
        final long sdt = command.getStartDateTime();
        final long edt = command.getEndDateTime();
        @Cleanup final IndexReader indexReader = queryService.indexReader(SilkCarRecord.class);
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(bqBuilder.build(), Integer.MAX_VALUE);
        final Stream<String> idStream = Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> Jlucene.toDocument(searcher, scoreDoc))
                .map(it -> it.get("id"));
        List<Document> list = Flux.fromStream(Stream.concat(idStream, RedisUtil.getAllSilkCarRecords()))
                .flatMap(id -> QueryService.find(SilkCarRecord.class, id))
                .filter(document -> {
                    String eventsString = document.getString("events");
                    if (!J.isBlank(eventsString)) {
                        final JsonNode events;
                        try {
                            events = MAPPER.readTree(eventsString);
                            Iterable<JsonNode> iterable = () -> events.elements();
                            return StreamSupport.stream(iterable.spliterator(), true)
                                    .anyMatch(event -> {
                                        String type = event.get("type").asText();
                                        long fireDateTime = event.get("fireDateTime").asLong();
                                        if ("PackageBoxEvent".equals(type)) {
                                            return fireDateTime > sdt && fireDateTime < edt;
                                        } else if ("JikonAdapterSilkCarInfoFetchEvent".equals(type)) {
                                            return fireDateTime > sdt && fireDateTime < edt;
                                        } else if ("TemporaryBoxEvent".equals(type)) {
                                            return fireDateTime > sdt && fireDateTime < edt;
                                        } else if ("SmallPackageBoxEvent".equals(type)) {
                                            return fireDateTime > sdt && fireDateTime < edt;
                                        }
                                        return false;
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String code = QueryService.find(SilkCar.class, document.getString("silkCar")).block().getString("code");
                        Map<String, String> silkCarMap = RedisUtil.getRedis(code);
                        return silkCarMap.keySet().stream()
                                .filter(it -> it.startsWith(RedisUtil.EVENT_SOURCE_KEY_PREFIX))
                                .map(key -> silkCarMap.get(key))
                                .flatMap(content -> {
                                    try {
                                        return Stream.of(MAPPER.readTree(content));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return Stream.empty();
                                })
                                .filter(event -> {
                                    String type = event.get("type").asText();
                                    return "PackageBoxEvent".equals(type) || "JikonAdapterSilkCarInfoFetchEvent".equals(type) || "TemporaryBoxEvent".equals(type) || "SmallPackageBoxEvent".equals(type);
                                })
                                .anyMatch(event -> {
                                    long fireDateTime = event.get("fireDateTime").asLong();
                                    return fireDateTime > sdt && fireDateTime < edt;
                                });
                    }
                    return false;
                })
                .flatMap(document -> {
                    final String initEvents = document.getString("initEvent");
                    Flowable<Document> flowable = Flowable.empty();
                    if (!J.isBlank(initEvents)) {
                        final JsonNode jsonNode;
                        try {
                            jsonNode = MAPPER.readTree(initEvents);
                            flowable = Flowable.fromIterable(jsonNode.get("silkRuntimes"))
                                    .map(silkRuntime -> QueryService.find(Silk.class, silkRuntime.get("silk").get("id").asText()).block());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return flowable;
                })
                .map(document -> {
                    Document lineMachine = QueryService.find(LineMachine.class, document.getString("lineMachine")).block();
                    Document line = QueryService.find(Line.class, lineMachine.getString("line")).block();
                    Document batch = QueryService.find(Batch.class, document.getString("batch")).block();
                    return document.append("batch", batch).append("line", line);
                })
                .filter(document ->
                {
                    if (!J.isBlank(command.getWorkshopId())) {
                        return command.getWorkshopId().equals(((Document) document.get("line")).getString("workshop"));
                    }
                    return true;
                })
                .collectList()
                .block();
        Single.just(list)
                .map(it -> new SilkExceptionReport(list))
                .map(MAPPER::writeValueAsString)
                .subscribe(reply::reply, err -> reply.fail(400, err.getMessage()));
    }
}
