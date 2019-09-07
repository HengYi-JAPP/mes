package com.hengyi.japp.mes.auto.report.application.internal;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.query.SilkCarRecordQuery;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.report.Jlucene;
import com.hengyi.japp.mes.auto.report.application.QueryService;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-05-20
 */
@Singleton
public class QueryServiceImpl implements QueryService {
    private final MesAutoConfig mesAutoConfig;

    @Inject
    private QueryServiceImpl(MesAutoConfig mesAutoConfig) {
        this.mesAutoConfig = mesAutoConfig;
    }

    @SneakyThrows
    @Override
    public IndexReader indexReader(Class clazz) {
        final Path path = mesAutoConfig.luceneIndexPath(clazz);
        return DirectoryReader.open(FSDirectory.open(path));
    }

    @SneakyThrows
    @Override
    public Collection<String> query(SilkCarRecordQuery silkCarRecordQuery) {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        Optional.ofNullable(silkCarRecordQuery.getWorkshopId()).filter(J::nonBlank)
                .ifPresent(it -> Jlucene.add(bqBuilder, "workshop", it));
        final long startL = J.date(silkCarRecordQuery.getStartDate()).getTime();
        final long endL = J.date(silkCarRecordQuery.getEndDate().plusDays(1)).getTime() - 1;
        bqBuilder.add(LongPoint.newRangeQuery("startDateTime", startL, endL), BooleanClause.Occur.MUST);

        @Cleanup final IndexReader indexReader = indexReader(SilkCarRecord.class);
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(bqBuilder.build(), Integer.MAX_VALUE);
        return Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> Jlucene.toDocument(searcher, scoreDoc))
                .map(it -> it.get("id"))
                .collect(toList());
    }

    @SneakyThrows
    @Override
    public Collection<String> querySilkCarRecordIds(String workshopId, long startL, long endL) {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        Optional.ofNullable(workshopId).filter(J::nonBlank)
                .ifPresent(it -> Jlucene.add(bqBuilder, "workshop", it));
        bqBuilder.add(LongPoint.newRangeQuery("startDateTime", startL, endL), BooleanClause.Occur.MUST);

        @Cleanup final IndexReader indexReader = indexReader(SilkCarRecord.class);
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(bqBuilder.build(), Integer.MAX_VALUE);
        return Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> Jlucene.toDocument(searcher, scoreDoc))
                .map(it -> it.get("id"))
                .collect(toList());
    }

    @SneakyThrows
    @Override
    public Collection<String> querySilkCarRecordIdsByEventSourceCanHappen(String workshopId, long startL, long endL) {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        Optional.ofNullable(workshopId).filter(J::nonBlank)
                .ifPresent(it -> Jlucene.add(bqBuilder, "workshop", it));
        final long currentL = new Date().getTime();
        bqBuilder.add(LongPoint.newRangeQuery("startDateTime", endL, currentL), BooleanClause.Occur.MUST_NOT);
        bqBuilder.add(LongPoint.newRangeQuery("endDateTime", 0, startL), BooleanClause.Occur.MUST_NOT);

        @Cleanup final IndexReader indexReader = indexReader(SilkCarRecord.class);
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(bqBuilder.build(), Integer.MAX_VALUE);
        return Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> Jlucene.toDocument(searcher, scoreDoc))
                .map(it -> it.get("id"))
                .collect(toList());
    }
}
