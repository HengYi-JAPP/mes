package com.hengyi.japp.mes.auto.search.lucene;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.query.DyeingPrepareQuery;
import com.hengyi.japp.mes.auto.application.query.DyeingPrepareResultQuery;
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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jzb 2018-06-25
 */
@Slf4j
@Singleton
public class DyeingPrepareLucene extends BaseLucene<DyeingPrepare> {

    @Inject
    private DyeingPrepareLucene(MesAutoConfig luceneRootPath) throws IOException {
        super(luceneRootPath);
    }

    @Override
    protected FacetsConfig facetsConfig() {
        final FacetsConfig result = new FacetsConfig();
        result.setIndexFieldName("corporation", "corporation");
        result.setIndexFieldName("workshop", "workshop");
        result.setIndexFieldName("batch", "batch");
        result.setIndexFieldName("type", "type");
        return result;
    }

    protected Document document(DyeingPrepare dyeingPrepare) {
        Document doc = new Document();
        doc.add(new StringField("id", dyeingPrepare.getId(), Field.Store.YES));

        doc.add(new StringField("type", dyeingPrepare.getType().name(), Field.Store.NO));
        doc.add(new FacetField("type", dyeingPrepare.getType().name()));

        Stream.concat(
                dyeingPrepare.prepareSilkCarRecords().stream()
                        .peek(silkCarRecord -> {
                            doc.add(new StringField("silkCarRecords", silkCarRecord.getId(), Field.Store.NO));
                            final SilkCar silkCar = silkCarRecord.getSilkCar();
                            doc.add(new StringField("silkCars", silkCar.getId(), Field.Store.NO));
                            doc.add(new StringField("silkCarCodes", silkCar.getCode(), Field.Store.NO));
                        })
                        .map(SilkCarRecord::getBatch),
                dyeingPrepare.prepareSilks().stream()
                        .peek(silk -> doc.add(new StringField("silks", silk.getId(), Field.Store.NO)))
                        .map(Silk::getBatch)
        ).distinct().forEach(batch -> {
            doc.add(new StringField("batch", batch.getId(), Field.Store.NO));
            final Workshop workshop = batch.getWorkshop();
            doc.add(new StringField("workshop", workshop.getId(), Field.Store.NO));
        });

        dyeingPrepare.prepareSilks().stream().map(Silk::getDoffingNum).collect(Collectors.toSet()).forEach(it -> {
            doc.add(new StringField("doffingNums", it, Field.Store.NO));
        });
        dyeingPrepare.prepareSilks().stream().map(Silk::getLineMachine).collect(Collectors.toSet()).forEach(it -> {
            doc.add(new StringField("lineMachines", it.getId(), Field.Store.NO));
        });

        addOperator(doc, "creator", dyeingPrepare.getCreator());
        addDateTime(doc, "createDateTime", dyeingPrepare.getCreateDateTime());

        addBoolean(doc, "submitted", dyeingPrepare.isSubmitted());
        addOperator(doc, "submitter", dyeingPrepare.getSubmitter());
        addDateTime(doc, "submitDateTime", dyeingPrepare.getSubmitDateTime());
        return doc;
    }

    public Query build(DyeingPrepareQuery dyeingQuery) {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        addQuery(bqBuilder, "submitted", false);

        addQuery(bqBuilder, "workshop", dyeingQuery.getWorkshopId());
        addQuery(bqBuilder, "silkCars", dyeingQuery.getSilkCarId());
        addQuery(bqBuilder, "doffingNums", dyeingQuery.getDoffingNum());
        addQuery(bqBuilder, "lineMachines", dyeingQuery.getLineMachineId());

        long startL = dyeingQuery.getStartDateTimestamp();
        long endL = dyeingQuery.getEndDateTimestamp();
        bqBuilder.add(LongPoint.newRangeQuery("createDateTime", startL, endL), BooleanClause.Occur.MUST);

        Optional.ofNullable(dyeingQuery.getHrIdQ())
                .filter(J::nonBlank)
                .map(it -> "*" + it)
                .ifPresent(it -> bqBuilder.add(new WildcardQuery(new Term("creator.hrId", it)), BooleanClause.Occur.MUST));
        return bqBuilder.build();
    }

    public Query build(DyeingPrepareResultQuery dyeingQuery) {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        addQuery(bqBuilder, "submitted", true);

        addQuery(bqBuilder, "workshop", dyeingQuery.getWorkshopId());
        addQuery(bqBuilder, "lineMachines", dyeingQuery.getLineMachineId());
        addQuery(bqBuilder, "silkCars", dyeingQuery.getSilkCarId());

        long startL = dyeingQuery.getStartDateTimestamp();
        long endL = dyeingQuery.getEndDateTimestamp();
        bqBuilder.add(LongPoint.newRangeQuery("submitDateTime", startL, endL), BooleanClause.Occur.MUST);

        Optional.ofNullable(dyeingQuery.getHrIdQ())
                .filter(J::nonBlank)
                .map(it -> "*" + it)
                .ifPresent(it -> bqBuilder.add(new WildcardQuery(new Term("creator.hrId", it)), BooleanClause.Occur.MUST));
        return bqBuilder.build();
    }

}
