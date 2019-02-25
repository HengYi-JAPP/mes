package com.hengyi.japp.mes.auto.search.lucene;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.query.SilkCarRecordQuery;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.Workshop;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * todo 索引更新
 *
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class SilkCarRecordLucene extends BaseLucene<SilkCarRecord> {

    @Inject
    private SilkCarRecordLucene(MesAutoConfig config) {
        super(config);
    }

    @Override
    protected FacetsConfig facetsConfig() {
        final FacetsConfig result = new FacetsConfig();
        result.setIndexFieldName("workshop", "workshop");
        result.setIndexFieldName("batch", "batch");
        result.setIndexFieldName("doffingType", "doffingType");
        result.setIndexFieldName("silkCar", "silkCar");
        result.setIndexFieldName("silkCarCode", "silkCarCode");
        return result;
    }

    public Query build(SilkCarRecordQuery silkCarRecordQuery) {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        bqBuilder.add(new TermQuery(new Term("silkCarCode", silkCarRecordQuery.getSilkCarCode())), BooleanClause.Occur.MUST);
        final long startL = J.date(silkCarRecordQuery.getStartDate()).getTime();
        final long endL = Optional.ofNullable(silkCarRecordQuery.getEndDate())
                .map(it -> it.plusDays(1))
                .map(J::date)
                .map(Date::getTime)
                .orElse(startL);
        bqBuilder.add(LongPoint.newRangeQuery("startDateTime", startL, endL), BooleanClause.Occur.MUST);
        return bqBuilder.build();
    }

    protected Document document(SilkCarRecord silkCarRecord) {
        Document doc = new Document();
        doc.add(new StringField("id", silkCarRecord.getId(), Field.Store.YES));

        final SilkCar silkCar = silkCarRecord.getSilkCar();
        doc.add(new StringField("silkCar", silkCar.getId(), Field.Store.NO));
        doc.add(new FacetField("silkCar", silkCar.getId()));
        doc.add(new StringField("silkCarCode", silkCar.getCode(), Field.Store.NO));
        doc.add(new FacetField("silkCarCode", silkCar.getCode()));

        final Batch batch = silkCarRecord.getBatch();
        doc.add(new StringField("batch", batch.getId(), Field.Store.NO));
        doc.add(new FacetField("batch", batch.getId()));

        final Workshop workshop = batch.getWorkshop();
        doc.add(new StringField("workshop", workshop.getId(), Field.Store.NO));
        doc.add(new FacetField("workshop", workshop.getId()));

        addDateTime(doc, "startDateTime", silkCarRecord.getStartDateTime());
        Optional.ofNullable(silkCarRecord.getEndDateTime())
                .ifPresent(it -> addDateTime(doc, "endDateTime", it));
        return doc;
    }

    public Collection<String> query(SilkCarRecordQuery silkCarRecordQuery) throws IOException {
        return baseQuery(build(silkCarRecordQuery));
    }
}
