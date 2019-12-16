package com.hengyi.japp.mes.auto.report.application.ExceptionRecordByClassReport;

import com.google.inject.Inject;
import com.hengyi.japp.mes.auto.domain.ExceptionRecord;
import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.SilkException;
import com.hengyi.japp.mes.auto.report.Report;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import com.hengyi.japp.mes.auto.report.application.SilkExceptionByClassReportService;
import com.hengyi.japp.mes.auto.report.application.command.ExceptionRecordByClassReportCommand;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.hengyi.japp.mes.auto.report.application.QueryService.findFromCache;
import static com.mongodb.client.model.Filters.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * @author jzb 2019-12-16
 */
public class SilkExceptionByClassReportServiceImpl implements SilkExceptionByClassReportService {
    private final QueryService queryService;
    private final MongoCollection<Document> T_ExceptionRecord;

    @Inject
    private SilkExceptionByClassReportServiceImpl(QueryService queryService) {
        this.queryService = queryService;
        T_ExceptionRecord = Report.mongoCollection(ExceptionRecord.class);
    }

    @Override
    public Mono<Collection<ReportItem>> report(ExceptionRecordByClassReportCommand command) {
        @NotBlank final String workshopId = command.getWorkshopId();
        @NotNull final Date startDateTime = command.getStartDateTime();
        @NotNull final Date endDateTime = command.getEndDateTime();
        return collectExceptionRecordInfo(workshopId, startDateTime, endDateTime)
                .flatMapIterable(Map::values)
                .map(groupBySilkException -> {
                    final SilkException silkException = groupBySilkException.getSilkException();
                    final List<ClassCodeItem> classCodeItems = Flux.fromIterable(groupBySilkException.getClassMap().values())
                            .map(groupByClass -> {
                                final String classCode = groupByClass.getClassCode();
                                final int silkCount = groupByClass.getSilkCount();

                                final ClassCodeItem classCodeItem = new ClassCodeItem();
                                classCodeItem.setClassCode(classCode);
                                classCodeItem.setSilkCount(silkCount);
                                return classCodeItem;
                            })
                            .toStream()
                            .collect(toUnmodifiableList());

                    final ReportItem reportItem = new ReportItem();
                    reportItem.setSilkException(silkException);
                    reportItem.setClassCodeItems(classCodeItems);
                    return reportItem;
                })
                .collect(toUnmodifiableList());
    }

    private Mono<Map<String, GroupBy_SilkException>> collectExceptionRecordInfo(String workshopId, @NotNull Date startDateTime, @NotNull Date endDateTime) {
        final Bson startFilter = gte("cdt", startDateTime);
        final Bson endFilter = lte("cdt", endDateTime);
        return Flux.from(T_ExceptionRecord.find(and(startFilter, endFilter))).reduceWith(ConcurrentHashMap::new, (acc, cur) -> {
            final String lineMachineId = cur.getString("lineMachine");
            final Document lineMachine = findFromCache(LineMachine.class, lineMachineId).get();
            final String lineId = lineMachine.getString("line");
            final Document line = findFromCache(Line.class, lineId).get();
            if (Objects.equals(workshopId, line.getString("workshop"))) {
                final String exceptionId = cur.getString("exception");
                acc.compute(exceptionId, (k, v) -> ofNullable(v).orElse(new GroupBy_SilkException(k)).collect(cur));
            }
            return acc;
        });
    }
}
