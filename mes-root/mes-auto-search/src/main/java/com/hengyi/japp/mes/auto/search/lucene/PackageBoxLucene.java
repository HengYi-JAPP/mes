package com.hengyi.japp.mes.auto.search.lucene;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQuery;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQueryForMeasure;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.*;
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
public class PackageBoxLucene extends BaseLucene<PackageBox> {

    @Inject
    private PackageBoxLucene(MesAutoConfig config) throws IOException {
        super(config);
    }

    @Override
    protected FacetsConfig facetsConfig() {
        final FacetsConfig result = new FacetsConfig();
        result.setIndexFieldName("type", "type");
        result.setIndexFieldName("workshop", "workshop");
        result.setIndexFieldName("product", "product");
        result.setIndexFieldName("batch", "batch");
        result.setIndexFieldName("grade", "grade");
        return result;
    }

    // todo 收集报表需求在决定
    protected Document document(PackageBox packageBox) {
        Document doc = new Document();
        doc.add(new StringField("id", packageBox.getId(), Field.Store.YES));
        Optional.ofNullable(packageBox.getCode())
                .filter(J::nonBlank)
                .ifPresent(it -> doc.add(new StringField("code", it, Field.Store.NO)));
        doc.add(new StringField("type", packageBox.getType().name(), Field.Store.NO));
        doc.add(new StringField("palletCode", J.defaultString(packageBox.getPalletCode()), Field.Store.NO));
        doc.add(new DoublePoint("netWeight", packageBox.getNetWeight()));
        doc.add(new DoublePoint("grossWeight", packageBox.getGrossWeight()));

        final Batch batch = packageBox.getBatch();
        doc.add(new StringField("batch", batch.getId(), Field.Store.NO));
        doc.add(new FacetField("batch", batch.getId()));

        final Product product = batch.getProduct();
        doc.add(new StringField("product", product.getId(), Field.Store.NO));
        doc.add(new FacetField("product", product.getId()));

        final Workshop workshop = batch.getWorkshop();
        doc.add(new StringField("workshop", workshop.getId(), Field.Store.NO));
        doc.add(new FacetField("workshop", workshop.getId()));

        final Grade grade = packageBox.getGrade();
        doc.add(new StringField("grade", grade.getId(), Field.Store.NO));
        doc.add(new FacetField("grade", grade.getId()));

        J.emptyIfNull(packageBox.getSilks()).stream().map(Silk::getId)
                .forEach(it -> doc.add(new StringField("silk", it, Field.Store.NO)));
        J.emptyIfNull(packageBox.getSilkCarRecords()).stream().forEach(silkCarRecord -> {
            doc.add(new StringField("silkCarRecord", silkCarRecord.getId(), Field.Store.NO));
            doc.add(new StringField("silkCarRecord.silkCar", silkCarRecord.getSilkCar().getId(), Field.Store.NO));
        });

        addDateTime(doc, "printDate", packageBox.getPrintDate());
        Optional.ofNullable(packageBox.getPrintClass()).map(PackageClass::getId)
                .ifPresent(it -> doc.add(new StringField("printClass", it, Field.Store.NO)));
        doc.add(new IntPoint("printCount", packageBox.getPrintCount()));

        addDateTime(doc, "budat", packageBox.getBudat());
        Optional.ofNullable(packageBox.getBudatClass()).map(PackageClass::getId)
                .ifPresent(it -> doc.add(new StringField("budatClass", it, Field.Store.NO)));
        Optional.ofNullable(packageBox.getSapT001l()).map(SapT001l::getId)
                .ifPresent(it -> doc.add(new StringField("sapT001l", it, Field.Store.NO)));
        addBoolean(doc, "inWarehouse", packageBox.isInWarehouse());
        addLoggable(doc, packageBox);
        return doc;
    }

    public Query build(PackageBoxQuery packageBoxQuery) {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        addQuery(bqBuilder, "inWarehouse", true);

        addQuery(bqBuilder, "workshop", packageBoxQuery.getWorkshopId());
        addQuery(bqBuilder, "batch", packageBoxQuery.getBatchId());
        addQuery(bqBuilder, "grade", packageBoxQuery.getGradeId());
        addQuery(bqBuilder, "product", packageBoxQuery.getProductId());
        addQuery(bqBuilder, "creator", packageBoxQuery.getCreatorId());
        addQuery(bqBuilder, "code", packageBoxQuery.getPackageBoxCode());
        addQuery(bqBuilder, "budatClass", packageBoxQuery.getBudatClassIds());
        addQuery(bqBuilder, "type", packageBoxQuery.getType());

        Optional.ofNullable(packageBoxQuery.getBudatRange()).ifPresent(it -> {
            final long startL = J.date(it.getStartLd()).getTime();
//            final long endL = J.date(it.getEndLd()).getTime();
            // 解决查询日期刚好前后日期时间相等
            final long endL = J.date(it.getEndLd()).getTime() - 1;
            bqBuilder.add(LongPoint.newRangeQuery("budat", startL, endL), BooleanClause.Occur.MUST);
        });

        return bqBuilder.build();
    }

    public Query build(PackageBoxQueryForMeasure packageBoxQuery) {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        addQuery(bqBuilder, "inWarehouse", false);

        addQuery(bqBuilder, "workshop", packageBoxQuery.getWorkshopId());
        addQuery(bqBuilder, "batch", packageBoxQuery.getBatchId());
        addQuery(bqBuilder, "grade", packageBoxQuery.getGradeId());
        addQuery(bqBuilder, "product", packageBoxQuery.getProductId());
        addQuery(bqBuilder, "creator", packageBoxQuery.getCreatorId());
        addQuery(bqBuilder, "code", packageBoxQuery.getPackageBoxCode());
        addQuery(bqBuilder, "type", packageBoxQuery.getType());

        Optional.ofNullable(packageBoxQuery.getNetWeight())
                .filter(it -> it > 0)
                .ifPresent(it -> bqBuilder.add(DoublePoint.newExactQuery("netWeight", it), BooleanClause.Occur.MUST));
        Optional.ofNullable(packageBoxQuery.getCreateDateTimeRange())
                .ifPresent(it -> {
                    final long startL = J.date(it.getStartLd()).getTime();
                    final long endL = J.date(it.getEndLd()).getTime();
                    bqBuilder.add(LongPoint.newRangeQuery("createDateTime", startL, endL), BooleanClause.Occur.MUST);
                });

        return bqBuilder.build();
    }
}
