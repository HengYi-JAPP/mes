package com.hengyi.japp.mes.auto.search.lucene;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.query.SilkQuery;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Optional;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class SilkLucene extends BaseLucene<Silk> {

    @Inject
    private SilkLucene(MesAutoConfig config) throws IOException {
        super(config);
    }

    @Override
    protected FacetsConfig facetsConfig() {
        final FacetsConfig result = new FacetsConfig();
        result.setIndexFieldName("corporation", "corporation");
        result.setIndexFieldName("workshop", "workshop");
        result.setIndexFieldName("line", "line");
        result.setIndexFieldName("lineMachine", "lineMachine");
        result.setIndexFieldName("batch", "batch");
        result.setIndexFieldName("grade", "grade");
        result.setIndexFieldName("doffingType", "doffingType");
        result.setIndexFieldName("silkCarRecords", "silkCarRecords");
        result.setMultiValued("silkCarRecords", true);
        return result;
    }

    // todo 收集报表需求在决定
    protected Document document(Silk silk) {
        Document doc = new Document();
        doc.add(new StringField("id", silk.getId(), Store.YES));
        doc.add(new StringField("code", silk.getCode(), Store.NO));

        final LineMachine lineMachine = silk.getLineMachine();
        doc.add(new StringField("lineMachine", lineMachine.getId(), Store.NO));
        doc.add(new FacetField("lineMachine", lineMachine.getId()));
        doc.add(new IntPoint("spindle", silk.getSpindle()));

        final Line line = lineMachine.getLine();
        doc.add(new StringField("line", line.getId(), Store.NO));
        doc.add(new FacetField("line", line.getId()));

        final Workshop workshop = line.getWorkshop();
        doc.add(new StringField("workshop", workshop.getId(), Store.NO));
        doc.add(new FacetField("workshop", workshop.getId()));

        final Corporation corporation = workshop.getCorporation();
        doc.add(new StringField("corporation", corporation.getId(), Store.NO));
        doc.add(new FacetField("corporation", corporation.getId()));

        final Batch batch = silk.getBatch();
        doc.add(new StringField("batch", batch.getId(), Store.NO));
        doc.add(new FacetField("batch", batch.getId()));

        final Grade grade = silk.getGrade();
        if (grade != null) {
            doc.add(new StringField("grade", grade.getId(), Store.NO));
            doc.add(new FacetField("grade", grade.getId()));
        }

        doc.add(new StringField("doffingType", silk.getDoffingType().name(), Store.NO));
        doc.add(new FacetField("doffingType", silk.getDoffingType().name()));
        addDateTime(doc, "doffingDateTime", silk.getDoffingDateTime());

        addBoolean(doc, "dyeingSample", silk.isDyeingSample());
        addBoolean(doc, "detached", silk.isDetached());

        CollectionUtils.emptyIfNull(silk.getSilkCarRecords()).stream().forEach(silkCarRecord -> {
            doc.add(new StringField("silkCarRecords", silkCarRecord.getId(), Store.NO));
            doc.add(new FacetField("silkCarRecords", silkCarRecord.getId()));
        });

        Optional.ofNullable(silk.getTemporaryBox())
                .map(TemporaryBox::getId)
                .ifPresent(it -> doc.add(new StringField("temporaryBox", it, Store.NO)));
        Optional.ofNullable(silk.getPackageBox())
                .map(PackageBox::getId)
                .ifPresent(it -> {
                    doc.add(new StringField("packageBox", it, Store.NO));
                    addDateTime(doc, "packageDateTime", silk.getPackageDateTime());
                });

        return doc;
    }

    public Query build(SilkQuery silkQuery) {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        addQuery(bqBuilder, "dyeingSample", silkQuery.isDyeingSample());
        addQuery(bqBuilder, "workshop", silkQuery.getWorkshopId());
        addQuery(bqBuilder, "batch", silkQuery.getBatchId());
        return bqBuilder.build();
    }
}
