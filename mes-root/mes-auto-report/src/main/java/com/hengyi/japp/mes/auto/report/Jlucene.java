package com.hengyi.japp.mes.auto.report;

import com.github.ixtf.japp.core.J;
import lombok.SneakyThrows;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.toSet;

/**
 * @author jzb 2019-05-29
 */
public class Jlucene {

    public static BooleanQuery.Builder add(BooleanQuery.Builder builder, String fieldName, boolean b) {
        builder.add(IntPoint.newExactQuery(fieldName, b ? 1 : 0), BooleanClause.Occur.MUST);
        return builder;
    }

    public static BooleanQuery.Builder add(BooleanQuery.Builder builder, String fieldName, Enum e) {
        Optional.ofNullable(e).map(Enum::name).ifPresent(it -> add(builder, fieldName, it));
        return builder;
    }

    public static BooleanQuery.Builder add(BooleanQuery.Builder builder, String fieldName, String s) {
        Optional.ofNullable(s).filter(J::nonBlank)
                .map(it -> new TermQuery(new Term(fieldName, s)))
                .ifPresent(it -> builder.add(it, BooleanClause.Occur.MUST));
        return builder;
    }

    public static BooleanQuery.Builder add(BooleanQuery.Builder builder, String fieldName, Collection<String> ss) {
        ss = J.emptyIfNull(ss).parallelStream().filter(J::nonBlank).collect(toSet());
        if (J.nonEmpty(ss)) {
            final BooleanQuery.Builder subBuilder = new BooleanQuery.Builder();
            ss.stream().map(it -> new TermQuery(new Term(fieldName, it)))
                    .forEach(it -> subBuilder.add(it, BooleanClause.Occur.SHOULD));
            builder.add(subBuilder.build(), BooleanClause.Occur.MUST);
        }
        return builder;
    }

    @SneakyThrows
    public static Document toDocument(IndexSearcher searcher, ScoreDoc scoreDoc) {
        return searcher.doc(scoreDoc.doc);
    }

}
