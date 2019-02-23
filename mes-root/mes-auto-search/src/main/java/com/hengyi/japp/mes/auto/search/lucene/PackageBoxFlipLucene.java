package com.hengyi.japp.mes.auto.search.lucene;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.query.PackageBoxFlipQuery;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Optional;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class PackageBoxFlipLucene extends BaseLucene<PackageBoxFlip> {

    @Inject
    private PackageBoxFlipLucene(MesAutoConfig config) throws IOException {
        super(config);
    }

    @Override
    protected FacetsConfig facetsConfig() {
        final FacetsConfig result = new FacetsConfig();
        result.setIndexFieldName("type", "type");
        result.setIndexFieldName("packageBox", "packageBox");
        result.setIndexFieldName("workshop", "workshop");
        result.setIndexFieldName("batch", "batch");
        result.setIndexFieldName("grade", "grade");
        return result;
    }

    // todo 收集报表需求在决定
    protected Document document(PackageBoxFlip packageBoxFlip) {
        Document doc = new Document();
        doc.add(new StringField("id", packageBoxFlip.getId(), Field.Store.YES));
        doc.add(new StringField("type", packageBoxFlip.getType().name(), Field.Store.NO));

        final PackageBox packageBox = packageBoxFlip.getPackageBox();
        doc.add(new StringField("packageBox", packageBox.getId(), Field.Store.NO));
        doc.add(new FacetField("packageBox", packageBox.getId()));

        final Batch batch = packageBox.getBatch();
        doc.add(new StringField("batch", batch.getId(), Field.Store.NO));
        doc.add(new FacetField("batch", batch.getId()));

        final Workshop workshop = batch.getWorkshop();
        doc.add(new StringField("workshop", workshop.getId(), Field.Store.NO));
        doc.add(new FacetField("workshop", workshop.getId()));

        final Grade grade = packageBox.getGrade();
        doc.add(new StringField("grade", grade.getId(), Field.Store.NO));
        doc.add(new FacetField("grade", grade.getId()));

        J.emptyIfNull(packageBoxFlip.getInSilks()).stream().map(Silk::getId)
                .forEach(it -> doc.add(new StringField("inSilk", it, Field.Store.NO)));
        J.emptyIfNull(packageBoxFlip.getOutSilks()).stream().map(Silk::getId)
                .forEach(it -> doc.add(new StringField("outSilk", it, Field.Store.NO)));

        addDateTime(doc, "budat", packageBox.getBudat());
        Optional.ofNullable(packageBox.getBudatClass()).map(PackageClass::getId)
                .ifPresent(it -> doc.add(new StringField("budatClass", it, Field.Store.NO)));
        Optional.ofNullable(packageBox.getSapT001l()).map(SapT001l::getId)
                .ifPresent(it -> doc.add(new StringField("sapT001l", it, Field.Store.NO)));
        addLoggable(doc, packageBoxFlip);
        return doc;
    }


    public Query build(PackageBoxFlipQuery packageBoxFlipQuery) {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();

        addQuery(bqBuilder, "packageBox", packageBoxFlipQuery.getPackageBoxId());
        addQuery(bqBuilder, "workshop", packageBoxFlipQuery.getWorkshopId());
        addQuery(bqBuilder, "batch", packageBoxFlipQuery.getBatchId());
        addQuery(bqBuilder, "grade", packageBoxFlipQuery.getGradeId());
        addQuery(bqBuilder, "type", packageBoxFlipQuery.getPackageBoxFlipType());

        final long startL = J.date(packageBoxFlipQuery.getStartLd()).getTime();
        final long endL = J.date(packageBoxFlipQuery.getEndLd()).getTime();
        bqBuilder.add(LongPoint.newRangeQuery("createDateTime", startL, endL), BooleanClause.Occur.MUST);

        return bqBuilder.build();
    }
}
