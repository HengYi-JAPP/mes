package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.SilkBarcodeQuery;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.SilkBarcode;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import java.time.LocalDate;

/**
 * @author jzb 2018-06-24
 */
public interface SilkBarcodeRepository {

    Single<SilkBarcode> create();

    Single<SilkBarcode> save(SilkBarcode silkBarcode);

    Single<SilkBarcode> find(String id);

    Single<SilkBarcode> findByCode(String code);

    /**
     * @param codeLd      落丝日期
     * @param lineMachine 机台
     * @param doffingNum  人工输入的落次
     * @param batch       批号
     */
    Maybe<SilkBarcode> find(LocalDate codeLd, LineMachine lineMachine, String doffingNum, Batch batch);

    /**
     * @param codeLd         落丝日期
     * @param codeDoffingNum 落次流水
     * @return
     */
    Single<SilkBarcode> find(LocalDate codeLd, long codeDoffingNum);

    Single<SilkBarcodeQuery.Result> query(SilkBarcodeQuery silkBarcodeQuery);

    Flowable<SilkBarcode> list();

    void index(SilkBarcode silkBarcode);

    Completable delete(String id);
}
