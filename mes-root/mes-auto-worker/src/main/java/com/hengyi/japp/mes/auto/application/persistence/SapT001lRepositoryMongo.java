package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.domain.SapT001l;
import com.hengyi.japp.mes.auto.repository.SapT001lRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class SapT001lRepositoryMongo extends MongoEntityRepository<SapT001l> implements SapT001lRepository {

    @Inject
    private SapT001lRepositoryMongo(MongoEntiyManager mongoEntiyManager) {
        super(mongoEntiyManager);
    }

    @Override
    public Single<SapT001l> save(SapT001l sapT001l) {
        sapT001l.setId(sapT001l.getLgort());
        return super.save(sapT001l);
    }
}
