package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.domain.SilkCarRecordDestination;
import com.hengyi.japp.mes.auto.repository.SilkCarRecordDestinationRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class SilkCarRecordDestinationRepositoryMongo extends MongoEntityRepository<SilkCarRecordDestination> implements SilkCarRecordDestinationRepository {

    @Inject
    private SilkCarRecordDestinationRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

}
