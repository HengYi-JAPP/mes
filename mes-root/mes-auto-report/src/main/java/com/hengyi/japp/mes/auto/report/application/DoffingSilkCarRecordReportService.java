package com.hengyi.japp.mes.auto.report.application;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.report.Jlucene;
import com.hengyi.japp.mes.auto.report.application.dto.DoffingSilkCarRecordReport;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * @author jzb 2019-05-20
 */
@Slf4j
@Singleton
public class DoffingSilkCarRecordReportService {
    private final QueryService queryService;

    @Inject
    private DoffingSilkCarRecordReportService(MesAutoConfig mesAutoConfig, QueryService queryService) {
        this.queryService = queryService;
    }

    @SneakyThrows
    public Mono<DoffingSilkCarRecordReport> generate(String workshopId, LocalDate startLd, LocalDate endLd) {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        Jlucene.add(bqBuilder, "workshop", workshopId);
        final long startL = J.date(startLd).getTime();
        final long endL = J.date(endLd.plusDays(1)).getTime() - 1;
        bqBuilder.add(LongPoint.newRangeQuery("startDateTime", startL, endL), BooleanClause.Occur.MUST);

        @Cleanup final IndexReader indexReader = queryService.indexReader(SilkCarRecord.class);
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(bqBuilder.build(), Integer.MAX_VALUE);
        return Flux.fromArray(topDocs.scoreDocs)
                .map(scoreDoc -> Jlucene.toDocument(searcher, scoreDoc))
                .map(it -> it.get("id"))
                .collectList()
                .map(DoffingSilkCarRecordReport::new);
    }
}
