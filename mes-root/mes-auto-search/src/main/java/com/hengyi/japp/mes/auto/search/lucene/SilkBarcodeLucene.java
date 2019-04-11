package com.hengyi.japp.mes.auto.search.lucene;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.query.SilkBarcodeQuery;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class SilkBarcodeLucene extends BaseLucene<SilkBarcode> {

    @Inject
    private SilkBarcodeLucene(MesAutoConfig config) {
        super(config);
    }

    @Override
    protected FacetsConfig facetsConfig() {
        final FacetsConfig result = new FacetsConfig();
        result.setIndexFieldName("workshop", "workshop");
        result.setIndexFieldName("line", "line");
        result.setIndexFieldName("lineMachine", "lineMachine");
        result.setIndexFieldName("doffingNum", "doffingNum");
        result.setIndexFieldName("batch", "batch");
        return result;
    }

    protected Document document(SilkBarcode silkBarcode) {
        Document doc = new Document();
        doc.add(new StringField("id", silkBarcode.getId(), Field.Store.YES));

        final LineMachine lineMachine = silkBarcode.getLineMachine();
        doc.add(new StringField("lineMachine", lineMachine.getId(), Field.Store.NO));
        doc.add(new FacetField("lineMachine", lineMachine.getId()));

        final Line line = lineMachine.getLine();
        doc.add(new StringField("line", line.getId(), Field.Store.NO));
        doc.add(new FacetField("line", line.getId()));

        final Workshop workshop = line.getWorkshop();
        doc.add(new StringField("workshop", workshop.getId(), Field.Store.NO));
        doc.add(new FacetField("workshop", workshop.getId()));
        doc.add(new LongPoint("codeDoffingNum", silkBarcode.getCodeDoffingNum()));

        addDateTime(doc, "codeDate", silkBarcode.getCodeDate());

        doc.add(new StringField("doffingNum", silkBarcode.getDoffingNum(), Field.Store.NO));
        doc.add(new FacetField("doffingNum", silkBarcode.getDoffingNum()));

        final Batch batch = silkBarcode.getBatch();
        doc.add(new StringField("batch", batch.getId(), Field.Store.NO));
        doc.add(new FacetField("batch", batch.getId()));

        return doc;
    }

    public Query build(SilkBarcodeQuery silkBarcodeQuery) {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        final long startL = Optional.ofNullable(silkBarcodeQuery.getStartLd())
                .map(J::date)
                .map(Date::getTime)
                .orElse(-1l);
        final long endL = Optional.ofNullable(silkBarcodeQuery.getEndLd())
                .map(J::date)
                .map(Date::getTime)
                .orElse(-1l);
        if (startL > 0 && endL > 0) {
            bqBuilder.add(LongPoint.newRangeQuery("codeDate", startL, endL), BooleanClause.Occur.MUST);
        }
        if (silkBarcodeQuery.getCodeDoffingNum() > 0) {
            bqBuilder.add(LongPoint.newExactQuery("codeDoffingNum", silkBarcodeQuery.getCodeDoffingNum()), BooleanClause.Occur.MUST);
        }

        final Optional<TermQuery> lineMachineQuery = Optional.ofNullable(silkBarcodeQuery.getLineMachineId()).filter(J::nonBlank).map(it -> new TermQuery(new Term("lineMachine", it)));
        final Optional<TermQuery> lineQuery = Optional.ofNullable(silkBarcodeQuery.getLineId()).filter(J::nonBlank).map(it -> new TermQuery(new Term("line", it)));
        final Optional<TermQuery> workshopQuery = Optional.ofNullable(silkBarcodeQuery.getWorkshopId()).filter(J::nonBlank).map(it -> new TermQuery(new Term("workshop", it)));
        if (lineMachineQuery.isPresent()) {
            bqBuilder.add(lineMachineQuery.get(), BooleanClause.Occur.MUST);
        } else if (lineQuery.isPresent()) {
            bqBuilder.add(lineQuery.get(), BooleanClause.Occur.MUST);
        } else if (workshopQuery.isPresent()) {
            bqBuilder.add(workshopQuery.get(), BooleanClause.Occur.MUST);
        }

        addQuery(bqBuilder, "batch", silkBarcodeQuery.getBatchId());
        addQuery(bqBuilder, "doffingNum", silkBarcodeQuery.getDoffingNum());
        if (J.isBlank(silkBarcodeQuery.getDoffingNum())) {
            Optional.ofNullable(silkBarcodeQuery.getDoffingNumQ())
                    .filter(J::nonBlank)
                    .ifPresent(it -> {
                        it = it + "*";
                        bqBuilder.add(new WildcardQuery(new Term("doffingNum", it)), BooleanClause.Occur.MUST);
                    });
        }
        return bqBuilder.build();
    }

    public Collection<String> query(SilkBarcodeQuery silkBarcodeQuery) throws IOException {
        return baseQuery(build(silkBarcodeQuery));
    }
}
