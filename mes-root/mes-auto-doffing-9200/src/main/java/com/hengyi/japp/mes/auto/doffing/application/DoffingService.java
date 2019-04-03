package com.hengyi.japp.mes.auto.doffing.application;

import com.hengyi.japp.mes.auto.doffing.domain.AutoDoffingSilkCarRecordAdapt;
import com.hengyi.japp.mes.auto.doffing.domain.AutoDoffingSilkCarRecordAdaptHistory;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2019-03-08
 */
public interface DoffingService {
    Flowable<AutoDoffingSilkCarRecordAdapt> fetch();

    Single<AutoDoffingSilkCarRecordAdaptHistory> toHistory(AutoDoffingSilkCarRecordAdapt data);

    Single<String> toMessageBody(AutoDoffingSilkCarRecordAdapt data);

    Completable restore(String id);

    Completable clean();
}
