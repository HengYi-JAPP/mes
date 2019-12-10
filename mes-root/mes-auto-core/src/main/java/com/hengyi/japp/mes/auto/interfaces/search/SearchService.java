package com.hengyi.japp.mes.auto.interfaces.search;

import com.google.common.collect.Maps;
import com.google.inject.ImplementedBy;
import com.hengyi.japp.mes.auto.application.query.*;
import com.hengyi.japp.mes.auto.domain.DyeingPrepare;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.SilkBarcode;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.interfaces.search.internal.LuceneCommandOne;
import com.hengyi.japp.mes.auto.interfaces.search.internal.SearchServiceImpl;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

/**
 * @author jzb 2019-11-14
 */
@ImplementedBy(SearchServiceImpl.class)
public interface SearchService {

    Mono<Void> index(LuceneCommandOne command);

    Mono<Void> remove(LuceneCommandOne command);

    Pair<Long, Collection<String>> query(String uri, Object query);

    String url(String url);

    default void index(PackageBox packageBox) {
        final LuceneCommandOne command = new LuceneCommandOne();
        command.setClassName(PackageBox.class);
        command.setId(packageBox.getId());
        index(command).subscribe();
    }

    default Pair<Long, Collection<String>> query(PackageBoxQueryOld query) {
        final Map<String, Object> map = Maps.newHashMap();
        map.put("first", query.getFirst());
        map.put("pageSize", query.getPageSize());
        map.put("workshopId", query.getWorkshopId());
        map.put("batchId", query.getBatchId());
        map.put("productId", query.getProductId());
        map.put("gradeId", query.getGradeId());
        map.put("type", query.getType());
        map.put("smallBatchId", query.getSmallBatchId());
        map.put("budatClassIds", query.getBudatClassIds());
        map.put("creatorId", query.getCreatorId());
        map.put("packageBoxCode", query.getPackageBoxCode());
        return query(url("packageBoxes"), map);
    }

    default Pair<Long, Collection<String>> query(PackageBoxQuery query) {
        return query(url("packageBoxes"), query);
    }

    default void index(SilkBarcode silkBarcode) {
        final LuceneCommandOne command = new LuceneCommandOne();
        command.setClassName(SilkBarcode.class);
        command.setId(silkBarcode.getId());
        index(command).subscribe();
    }

    default Pair<Long, Collection<String>> query(SilkBarcodeQuery query) {
        return query(url("silkBarcodes"), query);
    }

    default void index(SilkCarRecord silkCarRecord) {
        final LuceneCommandOne command = new LuceneCommandOne();
        command.setClassName(SilkCarRecord.class);
        command.setId(silkCarRecord.getId());
        index(command).subscribe();
    }

    default Pair<Long, Collection<String>> query(SilkCarRecordQuery query) {
        return query(url("silkCarRecords"), query);
    }

    Collection<String> query(SilkCarRecordQueryByEventSourceCanHappen query);

    default void index(DyeingPrepare dyeingPrepare) {
        final LuceneCommandOne command = new LuceneCommandOne();
        command.setClassName(DyeingPrepare.class);
        command.setId(dyeingPrepare.getId());
        index(command).subscribe();
    }

    default Pair<Long, Collection<String>> query(DyeingPrepareQuery query) {
        return query(url("dyeingPrepares"), query);
    }

}
