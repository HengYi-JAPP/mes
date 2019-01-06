package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.application.query.FormConfigQuery;
import com.hengyi.japp.mes.auto.domain.FormConfig;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface FormConfigRepository {

    Single<FormConfig> create();

    Single<FormConfig> save(FormConfig formConfig);

    Single<FormConfig> find(String id);

    Flowable<FormConfig> autoComplete(String q);

    Single<FormConfigQuery.Result> query(FormConfigQuery formConfigQuery);
}
