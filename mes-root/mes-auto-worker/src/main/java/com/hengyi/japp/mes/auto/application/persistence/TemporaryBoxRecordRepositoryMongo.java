package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.domain.TemporaryBox;
import com.hengyi.japp.mes.auto.domain.TemporaryBoxRecord;
import com.hengyi.japp.mes.auto.repository.TemporaryBoxRecordRepository;
import com.hengyi.japp.mes.auto.repository.TemporaryBoxRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class TemporaryBoxRecordRepositoryMongo extends MongoEntityRepository<TemporaryBoxRecord> implements TemporaryBoxRecordRepository {
    private final TemporaryBoxRepository temporaryBoxRepository;

    @Inject
    private TemporaryBoxRecordRepositoryMongo(MongoEntiyManager mongoEntiyManager, TemporaryBoxRepository temporaryBoxRepository) {
        super(mongoEntiyManager);
        this.temporaryBoxRepository = temporaryBoxRepository;
    }

    @Override
    public Single<TemporaryBoxRecord> save(TemporaryBoxRecord temporaryBoxRecord) {
        return super.save(temporaryBoxRecord).doOnSuccess(it -> {
            final TemporaryBox temporaryBox = it.getTemporaryBox();
            temporaryBoxRepository.rxInc(temporaryBox, temporaryBoxRecord.getCount()).subscribe();
        });
    }
}
