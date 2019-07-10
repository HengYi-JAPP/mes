package com.hengyi.japp.mes.auto.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.hengyi.japp.mes.auto.report.application.command.PackagePlanCommand;
import com.hengyi.japp.mes.auto.report.application.dto.packagePlanReport;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
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
import java.util.TimeZone;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author liuyuan
 * @create 2019-06-30 10:49
 * @description 包装计划排程
 **/
@Slf4j
@Singleton
public class PackagePlanService {
    private final QueryService queryService;

    @Inject
    public PackagePlanService(QueryService queryService) {
        this.queryService = queryService;
    }

    @SneakyThrows
    public void packagePlanBoard(Message<Object> reply) {
        PackagePlanCommand command = PackagePlanCommand.packagePlanCommand(reply.body());
        BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        Jlucene.add(bqBuilder, "workshop", command.getWorkshopId());
        final long endL = System.currentTimeMillis();
        final long startL = endL / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
        bqBuilder.add(LongPoint.newRangeQuery("startDateTime", 0, endL), BooleanClause.Occur.MUST);
        bqBuilder.add(LongPoint.newRangeQuery("endDateTime", startL, Long.MAX_VALUE), BooleanClause.Occur.MUST);
        @Cleanup final IndexReader indexReader = queryService.indexReader(SilkCarRecord.class);
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(bqBuilder.build(), Integer.MAX_VALUE);
        final Stream<String> idStream = Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> Jlucene.toDocument(searcher, scoreDoc))
                .map(it -> it.get("id"));
        final MongoCollection<Document> collection = Report.mongoCollection(Batch.class);
        final List<Document> batches = Flux.from(collection.find(Filters.eq("workshop", command.getWorkshopId()))).collectList().block();
        Stream<Map<String, String>> aLlSilkCarRecordsEvents = RedisUtil.getALlSilkCarRecordsEvents();
        //处理Redis中数据
        Flux<Document> redisFlux = Flux.fromStream(aLlSilkCarRecordsEvents)
                .flatMap(redisMap -> QueryService.find(SilkCarRecord.class, redisMap.get("silkCarRecord"))
                        .filter(silkCarRecord -> batches.stream().anyMatch(batch -> silkCarRecord.getString("batch").equals(batch.getString("_id"))))
                        .map(document -> document.append("redisEvents", redisMap)));
        //处理MongoDB中数据
        Flux<Document> mongoFlux = Flux.fromStream(idStream)
                .flatMap(id -> QueryService.find(SilkCarRecord.class, id));
        List<Document> documentList = Flux.concat(redisFlux, mongoFlux)
                .map(document -> {
                    Document batch = batches.parallelStream().filter(it -> it.getString("_id").equals(document.getString("batch"))).findFirst().orElse(null);
                    return document.append("batch", batch);
                })
                .map(document -> {
                    String eventsString = document.getString("events");
                    if (!J.isBlank(eventsString)) {
                        final JsonNode events;
                        try {
                            events = MAPPER.readTree(eventsString);
                            Iterable<JsonNode> iterable = () -> events.elements();
                            boolean anyMatch = StreamSupport.stream(iterable.spliterator(), true).anyMatch(event -> {
                                String type = event.get("type").asText();
                                long fireDateTime = event.get("fireDateTime").asLong();
                                if ("PackageBoxEvent".equals(type)) {
                                    return fireDateTime > startL && fireDateTime < endL;
                                } else if ("RiambSilkCarInfoFetchEvent".equals(type)) {
                                    return fireDateTime > startL && fireDateTime < endL;
                                } else if ("SmallPackageBoxEvent".equals(type)) {
                                    return fireDateTime > startL && fireDateTime < endL;
                                }
                                return false;
                            });
                            if (anyMatch) {
                                return document.append("packageFlag", true);
                            }
                            return document.append("packageFlag", false);
                        } catch (Exception e) {
                            return document;
                        }
                    } else if (document.get("redisEvents") != null) {
                        Map<String, String> eventMap = (Map<String, String>) document.get("redisEvents");
                        boolean anyMatch = eventMap.keySet().stream()
                                .filter(it -> it.startsWith(RedisUtil.EVENT_SOURCE_KEY_PREFIX))
                                .map(key -> eventMap.get(key))
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
                                    return "PackageBoxEvent".equals(type) || "RiambSilkCarInfoFetchEvent".equals(type) || "SmallPackageBoxEvent".equals(type);
                                })
                                .anyMatch(event -> {
                                    long fireDateTime = event.get("fireDateTime").asLong();
                                    return fireDateTime > startL && fireDateTime < endL;
                                });
                        if (anyMatch) {
                            return document.append("packageFlag", true);
                        }
                        return document.append("packageFlag", false);
                    }
                    return document;
                })
                .map(document -> {
                    final String initEvents = document.getString("initEvent");
                    if (!J.isBlank(initEvents)) {
                        final JsonNode jsonNode;
                        try {
                            jsonNode = MAPPER.readTree(initEvents);
                            int silkCount = jsonNode.get("silkRuntimes").size();
                            return document.append("silkCount", silkCount);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return document.append("silkCount", 0);
                })
                .collectList()
                .block();
        Single.just(documentList)
                .map(list -> new packagePlanReport(list))
                .map(MAPPER::writeValueAsString)
                .subscribe(reply::reply, err -> reply.fail(400, err.getMessage()));
        ;
    }
}
