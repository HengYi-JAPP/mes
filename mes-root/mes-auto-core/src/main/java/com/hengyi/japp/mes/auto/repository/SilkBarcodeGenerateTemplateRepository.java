package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.SilkBarcodeGenerateTemplate;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface SilkBarcodeGenerateTemplateRepository {

    Single<SilkBarcodeGenerateTemplate> create();

    Single<SilkBarcodeGenerateTemplate> save(SilkBarcodeGenerateTemplate template);

    Flowable<SilkBarcodeGenerateTemplate> listByLineId(String lineId);

    default Flowable<SilkBarcodeGenerateTemplate> listBy(Line line) {
        return listByLineId(line.getId());
    }

}
