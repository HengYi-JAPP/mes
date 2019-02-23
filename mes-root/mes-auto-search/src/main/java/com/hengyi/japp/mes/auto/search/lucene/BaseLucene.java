package com.hengyi.japp.mes.auto.search.lucene;

/**
 * @author jzb 2018-08-24
 */

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.application.persistence.JsonEntity;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.domain.LoggableMongoEntity;
import com.hengyi.japp.mes.auto.domain.Operator;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseLucene<T extends JsonEntity> {
    protected final Class<T> entityClass;
    protected final IndexWriter indexWriter;
    protected final DirectoryTaxonomyWriter taxoWriter;
    protected final FacetsConfig facetsConfig;

    @SneakyThrows
    protected BaseLucene(MesAutoConfig config) {
        entityClass = entityClass();
        final Path indexPath = config.luceneIndexPath(entityClass);
        indexWriter = new IndexWriter(FSDirectory.open(indexPath), new IndexWriterConfig(new SmartChineseAnalyzer()));
        final Path taxoPath = config.luceneTaxoPath(entityClass);
        taxoWriter = new DirectoryTaxonomyWriter(FSDirectory.open(taxoPath));
        facetsConfig = facetsConfig();
    }

    public IndexReader indexReader() throws IOException {
        return DirectoryReader.open(indexWriter);
    }

    public DirectoryTaxonomyReader taxoReader() throws IOException {
        return new DirectoryTaxonomyReader(taxoWriter);
    }

    public void index(T entity) throws IOException {
        Term term = new Term("id", entity.getId());
        if (entity.isDeleted()) {
            indexWriter.deleteDocuments(term);
        } else {
            indexWriter.updateDocument(term, facetsConfig.build(taxoWriter, document(entity)));
        }
        indexWriter.commit();
        taxoWriter.commit();
    }

    public void delete(String id) throws IOException {
        Term term = new Term("id", id);
        indexWriter.deleteDocuments(term);
        indexWriter.commit();
        taxoWriter.commit();
    }

    protected void addBoolean(Document doc, String fieldName, boolean b) {
        doc.add(new IntPoint(fieldName, b ? 1 : 0));
    }

    protected void addQuery(BooleanQuery.Builder bqBuilder, String fieldName, boolean b) {
        bqBuilder.add(IntPoint.newExactQuery(fieldName, b ? 1 : 0), BooleanClause.Occur.MUST);
    }

    protected void addQuery(BooleanQuery.Builder bqBuilder, String fieldName, Enum e) {
        Optional.ofNullable(e).map(Enum::name)
                .map(it -> new TermQuery(new Term(fieldName, it)))
                .ifPresent(it -> bqBuilder.add(it, BooleanClause.Occur.MUST));
    }

    protected void addQuery(BooleanQuery.Builder bqBuilder, String fieldName, String s) {
        Optional.ofNullable(s).filter(J::nonBlank)
                .map(it -> new TermQuery(new Term(fieldName, s)))
                .ifPresent(it -> bqBuilder.add(it, BooleanClause.Occur.MUST));
    }

    protected void addQuery(BooleanQuery.Builder bqBuilder, String fieldName, Collection<String> ss) {
        if (J.nonEmpty(ss)) {
            final BooleanQuery.Builder subBqBuilder = new BooleanQuery.Builder();
            ss.stream().filter(J::nonBlank).forEach(it ->
                    subBqBuilder.add(new TermQuery(new Term(fieldName, it)), BooleanClause.Occur.SHOULD)
            );
            bqBuilder.add(subBqBuilder.build(), BooleanClause.Occur.MUST);
        }
    }

    protected void addOperator(Document doc, String fieldName, Operator operator) {
        Optional.ofNullable(operator)
                .map(Operator::getId)
                .ifPresent(it -> doc.add(new StringField(fieldName, it, Store.NO)));
        Optional.ofNullable(operator)
                .map(Operator::getHrId)
                .filter(J::nonBlank)
                .ifPresent(it -> doc.add(new StringField(fieldName + ".hrId", it, Store.YES)));
    }

    protected void addDateTime(Document doc, String fieldName, Date date) {
        Optional.ofNullable(date)
                .map(Date::getTime)
                .ifPresent(it -> {
                    doc.add(new LongPoint(fieldName, it));
                    doc.add(new NumericDocValuesField(fieldName, it));
                });
    }

    protected void addLoggable(Document doc, LoggableMongoEntity entity) {
        addOperator(doc, "creator", entity.getCreator());
        addDateTime(doc, "createDateTime", entity.getCreateDateTime());
        addOperator(doc, "modifier", entity.getModifier());
        addDateTime(doc, "modifyDateTime", entity.getModifyDateTime());
    }

    public Collection<String> baseQuery(Query query) throws IOException {
        @Cleanup final IndexReader indexReader = indexReader();
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
        return baseQuery(searcher, topDocs);
    }

    public Collection<String> baseQuery(Query query, Sort sort) throws IOException {
        @Cleanup final IndexReader indexReader = indexReader();
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE, sort);
        return baseQuery(searcher, topDocs);
    }

    private Collection<String> baseQuery(IndexSearcher searcher, TopDocs topDocs) {
        return Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> toDocument(searcher, scoreDoc))
                .map(it -> it.get("id"))
                .collect(Collectors.toList());
    }

    public Pair<Long, Collection<String>> baseQuery(Query query, int first, int pageSize) throws IOException {
        @Cleanup final IndexReader indexReader = indexReader();
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(query, first + pageSize);
        return baseQuery(searcher, topDocs, first);
    }

    public Pair<Long, Collection<String>> baseQuery(Query query, int first, int pageSize, Sort sort) throws IOException {
        @Cleanup final IndexReader indexReader = indexReader();
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(query, first + pageSize, sort);
        return baseQuery(searcher, topDocs, first);
    }

    private Pair<Long, Collection<String>> baseQuery(IndexSearcher searcher, TopDocs topDocs, int first) {
        if (topDocs.totalHits < 1) {
            return Pair.of(topDocs.totalHits, Collections.EMPTY_LIST);
        }
        final List<String> ids = Arrays.stream(topDocs.scoreDocs)
                .skip(first)
                .map(scoreDoc -> toDocument(searcher, scoreDoc))
                .map(it -> it.get("id"))
                .collect(Collectors.toList());
        return Pair.of(topDocs.totalHits, ids);
    }

    @SneakyThrows
    private Document toDocument(IndexSearcher searcher, ScoreDoc scoreDoc) {
        return searcher.doc(scoreDoc.doc);
    }

    public void close() throws IOException {
        taxoWriter.close();
        indexWriter.close();
    }

    private Class<T> entityClass() {
        ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    protected abstract FacetsConfig facetsConfig();

    protected abstract Document document(T entity);
}
