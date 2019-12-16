package com.hengyi.japp.mes.auto.report.application.SilkExceptionReport;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.report.Report;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.hengyi.japp.mes.auto.report.application.SilkExceptionReportService;
import com.hengyi.japp.mes.auto.report.application.command.ExceptionRecordReportCommand;
import com.hengyi.japp.mes.auto.report.application.dto.silk_car_record.SilkCarRecordAggregate;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.hengyi.japp.mes.auto.report.application.QueryService.ID_COL;
import static com.hengyi.japp.mes.auto.report.application.QueryService.findFromCache;
import static com.mongodb.client.model.Filters.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * @author jzb 2019-12-16
 */
@Slf4j
@Singleton
public class SilkExceptionReportServiceImpl implements SilkExceptionReportService {
    private final QueryService queryService;
    private final MongoCollection<Document> T_ExceptionRecord;

    @Inject
    private SilkExceptionReportServiceImpl(QueryService queryService) {
        this.queryService = queryService;
        T_ExceptionRecord = Report.mongoCollection(ExceptionRecord.class);
    }

    @Override
    public Mono<Collection<ReportItem>> report(ExceptionRecordReportCommand command) {
        @NotBlank final String workshopId = command.getWorkshopId();
        @NotNull final Date startDateTime = command.getStartDateTime();
        @NotNull final Date endDateTime = command.getEndDateTime();
        return collectExceptionRecordInfo(workshopId, startDateTime, endDateTime)
                .flatMap(it -> collectTotalAndGradeInfo(workshopId, startDateTime, endDateTime, it))
                .flatMapIterable(Map::values)
                .flatMap(groupByLine -> {
                    final Line line = groupByLine.getLine();
                    return Flux.fromIterable(groupByLine.getBatchMap().values()).map(groupByBatch -> {
                        final Batch batch = groupByBatch.getBatch();
                        final int silkCount_line_batch = groupByBatch.getSilkCount();
                        final List<SilkExceptionItem> silkExceptionItems = groupByBatch.getSilkExceptionMap().values().parallelStream().map(it -> {
                            final SilkException silkException = it.getSilkException();
                            final int silkCount = it.getSilkCount();
                            final SilkExceptionItem silkExceptionItem = new SilkExceptionItem();
                            silkExceptionItem.setSilkException(silkException);
                            silkExceptionItem.setSilkCount(silkCount);
                            return silkExceptionItem;
                        }).collect(toUnmodifiableList());
                        final List<GradeItem> gradeItems = groupByBatch.getGradeMap().values().parallelStream().map(it -> {
                            final Grade grade = it.getGrade();
                            final int silkCount = it.getSilkCount();
                            final GradeItem gradeItem = new GradeItem();
                            gradeItem.setGrade(grade);
                            gradeItem.setSilkCount(silkCount);
                            return gradeItem;
                        }).collect(toUnmodifiableList());

                        final ReportItem reportItem = new ReportItem();
                        reportItem.setLine(line);
                        reportItem.setBatch(batch);
                        reportItem.setSilkCount(silkCount_line_batch);
                        reportItem.setSilkExceptionItems(silkExceptionItems);
                        reportItem.setGradeItems(gradeItems);

                        final GroupBy_NoGrade noGrade = groupByBatch.getNoGrade();
                        if (noGrade.getSilkCount() > 0) {
                            final NoGradeInfo noGradeInfo = new NoGradeInfo();
                            noGradeInfo.setGrade(noGrade.getGrade());
                            noGradeInfo.setSilkCount(noGrade.getSilkCount());
                            final Collection<SilkCarRecordInfo> silkCarRecordInfos = noGrade.getSilkCarRecordMap().values()
                                    .parallelStream()
                                    .map(it -> {
                                        final Document silkCarBson = it.getSilkCar();
                                        final SilkCar silkCar = new SilkCar();
                                        silkCar.setId(silkCarBson.getString(ID_COL));
                                        silkCar.setCode(silkCarBson.getString("code"));

                                        final Document creatorBson = it.getCreator();
                                        final Operator creator = new Operator();
                                        creator.setId(creatorBson.getString(ID_COL));
                                        creator.setName(creatorBson.getString("name"));
                                        creator.setHrId(creatorBson.getString("hrId"));

                                        final SilkCarRecordInfo silkCarRecordInfo = new SilkCarRecordInfo();
                                        silkCarRecordInfo.setId(it.getId());
                                        silkCarRecordInfo.setType(it.getType());
                                        silkCarRecordInfo.setSilkCar(silkCar);
                                        silkCarRecordInfo.setStartDateTime(it.getStartDateTime());
                                        silkCarRecordInfo.setCreator(creator);
                                        return silkCarRecordInfo;
                                    })
                                    .collect(toUnmodifiableList());
                            noGradeInfo.setSilkCarRecordInfos(silkCarRecordInfos);
                            reportItem.setNoGradeInfo(noGradeInfo);
                        }
                        return reportItem;
                    });
                })
                .collect(toUnmodifiableList());
    }

    private Mono<Map<String, GroupBy_Line>> collectExceptionRecordInfo(String workshopId, @NotNull Date startDateTime, @NotNull Date endDateTime) {
        final Bson startFilter = gte("cdt", startDateTime);
        final Bson endFilter = lte("cdt", endDateTime);
        return Flux.from(T_ExceptionRecord.find(and(startFilter, endFilter))).reduceWith(ConcurrentHashMap::new, (acc, cur) -> {
            final String lineMachineId = cur.getString("lineMachine");
            final Document lineMachine = findFromCache(LineMachine.class, lineMachineId).get();
            final String lineId = lineMachine.getString("line");
            final Document line = findFromCache(Line.class, lineId).get();
            if (Objects.equals(workshopId, line.getString("workshop"))) {
                acc.compute(lineId, (k, v) -> ofNullable(v).orElse(new GroupBy_Line(line)).collect(cur));
            }
            return acc;
        });
    }

    private Mono<Map<String, GroupBy_Line>> collectTotalAndGradeInfo(String workshopId, @NotNull Date startDateTime, @NotNull Date endDateTime, Map<String, GroupBy_Line> groupByLineMap) {
        return Flux.fromIterable(queryService.querySilkCarRecordIds(workshopId, startDateTime.getTime(), endDateTime.getTime()))
                .flatMap(SilkCarRecordAggregate::from)
                .filter(it -> Objects.equals(DoffingType.AUTO, it.getDoffingType())
                        || Objects.equals(DoffingType.MANUAL, it.getDoffingType()))
                .reduce(groupByLineMap, (acc, cur) -> {
                    Flux.fromIterable(J.emptyIfNull(cur.getInitSilkRuntimeDtos()))
                            .map(SilkRuntime.DTO::getSilk)
                            .flatMap(it -> QueryService.find(Silk.class, it))
                            .toStream()
                            .forEach(silk -> {
                                final String lineMachineId = silk.getString("lineMachine");
                                final Document lineMachine = findFromCache(LineMachine.class, lineMachineId).get();
                                final String lineId = lineMachine.getString("line");
                                final Document line = findFromCache(Line.class, lineId).get();
                                acc.compute(lineId, (k, v) -> ofNullable(v).orElse(new GroupBy_Line(line)).collect(cur, silk));
                            });
                    return acc;
                });
    }
}
