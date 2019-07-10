package com.hengyi.japp.mes.auto.report.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.report.Jlucene;
import com.hengyi.japp.mes.auto.report.RedisUtil;
import com.hengyi.japp.mes.auto.report.application.command.MeasureFiberReportCommand;
import com.hengyi.japp.mes.auto.report.application.dto.measureFiber.MeasureFiberReport;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.core.eventbus.Message;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author liuyuan
 * @create 2019-05-29 18:16
 * @description
 **/
@Slf4j
@Singleton
public class MeasureFiberReportService {
    private final File baseDir;
    private final QueryService queryService;

    @Inject
    public MeasureFiberReportService(MesAutoConfig mesAutoConfig, QueryService queryService) {
        this.baseDir = mesAutoConfig.reportPath("measureFiberService").toFile();
        this.queryService = queryService;
    }

    private File file(Document workshop, LocalDateTime sdt, LocalDateTime edt) {
        final File dir = FileUtils.getFile(baseDir, "" + sdt, "" + edt);
        final String fileName = String.join(".", workshop.getString("code"), sdt.toString(), edt.toString());
        return FileUtils.getFile(dir, fileName);
    }

    @SneakyThrows
    public void measureFiberReport(Message<Object> reply) {
        MeasureFiberReportCommand command = MeasureFiberReportCommand.measureFiberReportCommand(reply.body());
        BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        Jlucene.add(bqBuilder, "workshop", command.getWorkshopId());
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final long startL = J.date(LocalDateTime.parse(command.getStartDateTime(), df)).getTime();
        final long endL = J.date(LocalDateTime.parse(command.getEndDateTime(), df)).getTime();
        final long currentStartL = J.date(LocalDateTime.parse(command.getStartDateTime(), df)).getTime();
        final long currentEndL = J.date(LocalDateTime.parse(command.getEndDateTime(), df)).getTime();
        bqBuilder.add(LongPoint.newRangeQuery("startDateTime", 0, endL), BooleanClause.Occur.MUST);
        bqBuilder.add(LongPoint.newRangeQuery("endDateTime", startL, Long.MAX_VALUE), BooleanClause.Occur.MUST);
        @Cleanup final IndexReader indexReader = queryService.indexReader(SilkCarRecord.class);
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(bqBuilder.build(), Integer.MAX_VALUE);
        final Stream<String> idStream = Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> Jlucene.toDocument(searcher, scoreDoc))
                .map(it -> it.get("id"));
        List<Document> list = Flux.fromStream(Stream.concat(idStream, RedisUtil.getAllSilkCarRecords()))
                .flatMap(id -> QueryService.find(SilkCarRecord.class, id))
                .map(document -> {
                    Document silkCar = QueryService.find(SilkCar.class, document.getString("silkCar")).block();
                    Document batch = QueryService.find(Batch.class, document.getString("batch")).block();
                    Document product = QueryService.find(Product.class, batch.getString("product")).block();
                    return document.append("code", silkCar.getString("code")).append("silkCar", silkCar).append("batch", batch).append("product", product);
                })
                .filter(document -> {
                    if (!J.isBlank(command.getWorkshopId())) {
                        return command.getWorkshopId().equals(((Document) document.get("batch")).getString("workshop"));
                    }
                    return true;
                })
                .collectList()
                .block();
        Single.just(list)
                .flatMapPublisher(Flowable::fromIterable)
                .flatMap(document -> {
                    final String eventsJsonString = document.getString("events");
                    Flowable<JsonNode> flowable = Flowable.empty();
                    if (!J.isBlank(eventsJsonString)) {
                        final JsonNode jsonNode = MAPPER.readTree(eventsJsonString);
                        flowable = Flowable.fromIterable(jsonNode);
                    } else {
                        Map<String, String> silkCarMap = RedisUtil.getRedis(document.getString("code"));
                        if (silkCarMap != null) {
                            flowable = Flowable.fromIterable(silkCarMap.keySet())
                                    .filter(it -> it.startsWith(RedisUtil.EVENT_SOURCE_KEY_PREFIX))
                                    .map(key -> silkCarMap.get(key))
                                    .map(MAPPER::readTree);
                        }
                    }
                    return flowable.map(eventSource -> {
                        ObjectNode objectNode = (ObjectNode) eventSource;
                        if (objectNode.get("operator") != null && objectNode.get("operator").get("id") != null) {
                            Document operator = QueryService.find(Operator.class, objectNode.get("operator").get("id").asText()).block();
                            objectNode.put("operator", operator.getString("name"));
                        }
                        if (objectNode.get("silkNote") != null && objectNode.get("silkNote").get("id") != null) {
                            Document silkNote = QueryService.find(SilkNote.class, objectNode.get("silkNote").get("id").asText()).block();
                            objectNode.put("silkNote", silkNote.getString("name"));
                        }
                        if ("ProductProcessSubmitEvent".equals(objectNode.get("type").asText())) {
                            if (objectNode.get("productProcess") != null) {
                                Document productProcess = QueryService.find(ProductProcess.class, objectNode.get("productProcess").get("id").asText()).block();
                                if ("落筒异常".equals(productProcess.getString("name"))) {
                                    int silkCount = objectNode.get("silkRuntimes").size();
                                    document.append("silkCount", silkCount);
                                }
                            }
                        }
                        return objectNode;
                    })
                            .toList()
                            .map(eventSources -> new MeasureFiberReport.Item(eventSources, document, (Document) document.get("product"), document.getInteger("silkCount", 0)))
                            .flatMapPublisher(Flowable::just);
                })
                .filter(item -> item.getEventSources().parallelStream().anyMatch(eventSource ->
                        "SilkNoteFeedbackEvent".equals(eventSource.get("type").asText())
                                && eventSource.get("silkNote") != null
                                && "测纤".equals(eventSource.get("silkNote").asText())
                                && eventSource.get("fireDateTime").asLong() > currentStartL
                                && eventSource.get("fireDateTime").asLong() < currentEndL))
                .toList()
                .map(MeasureFiberReport::new)
                .map(MAPPER::writeValueAsString)
                .subscribe(reply::reply, err -> reply.fail(400, err.getMessage()));
    }
}
