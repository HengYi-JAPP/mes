package com.hengyi.japp.mes.auto.application.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntityRepository;
import com.hengyi.japp.mes.auto.application.persistence.proxy.MongoEntiyManager;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.SilkBarcodeGenerateTemplate;
import com.hengyi.japp.mes.auto.repository.LineMachineRepository;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeGenerateTemplateRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;

/**
 * @author jzb 2018-06-24
 */
@Slf4j
@Singleton
public class SilkBarcodeGenerateTemplateRepositoryMongo extends MongoEntityRepository<SilkBarcodeGenerateTemplate> implements SilkBarcodeGenerateTemplateRepository {
    private final LineMachineRepository lineMachineRepository;

    @Inject
    private SilkBarcodeGenerateTemplateRepositoryMongo(MongoEntiyManager mongoEntiyManager, LineMachineRepository lineMachineRepository) {
        super(mongoEntiyManager);
        this.lineMachineRepository = lineMachineRepository;
    }

    @Override
    public Single<SilkBarcodeGenerateTemplate> save(SilkBarcodeGenerateTemplate template) {
        final LineMachine lineMachine = template.getLineMachine();
        template.setId(lineMachine.getId());
        return super.save(template);
    }

    @Override
    public Flowable<SilkBarcodeGenerateTemplate> listByLineId(String lineId) {
        return lineMachineRepository.listByLineId(lineId).flatMapSingle(lineMachine -> {
            @NotBlank final String id = lineMachine.getId();
            final JsonObject query = new JsonObject().put("_id", id);
            return mongoClient.rxFindOne(collectionName, query, new JsonObject())
                    .switchIfEmpty(Single.just(new JsonObject()))
                    .flatMap(this::rxCreateMongoEntiy)
                    .map(it -> {
                        it.setId(id);
                        it.setLineMachine(lineMachine);
                        return it;
                    });
        });
    }
}
