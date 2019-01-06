package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.domain.SilkNote;
import com.hengyi.japp.mes.auto.repository.SilkNoteRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class SilkNoteRepositoryMongo extends MongoEntityRepository<SilkNote> implements SilkNoteRepository {

    @Inject
    private SilkNoteRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

}
