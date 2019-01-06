package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.BatchUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.repository.BatchRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.ProductRepository;
import com.hengyi.japp.mes.auto.repository.WorkshopRepository;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class BatchServiceImpl implements BatchService {
    private final BatchRepository batchRepository;
    private final WorkshopRepository workshopRepository;
    private final ProductRepository productRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private BatchServiceImpl(BatchRepository batchRepository, WorkshopRepository workshopRepository, ProductRepository productRepository, OperatorRepository operatorRepository) {
        this.batchRepository = batchRepository;
        this.workshopRepository = workshopRepository;
        this.productRepository = productRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<Batch> create(Principal principal, BatchUpdateCommand command) {
        return batchRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<Batch> save(Principal principal, Batch batch, BatchUpdateCommand command) {
        batch.setBatchNo(command.getBatchNo());
        batch.setCentralValue(command.getCentralValue());
        batch.setHoleNum(command.getHoleNum());
        batch.setNote(command.getNote());
        batch.setSpec(command.getSpec());
        batch.setTubeColor(command.getTubeColor());
        batch.setSilkWeight(command.getSilkWeight());

        return productRepository.find(command.getProduct().getId())
                .flatMap(product -> {
                    batch.setProduct(product);
                    return workshopRepository.find(command.getWorkshop().getId());
                })
                .flatMap(workshop -> {
                    batch.setWorkshop(workshop);
                    return operatorRepository.find(principal);
                })
                .flatMap(operator -> {
                    batch.log(operator);
                    return batchRepository.save(batch);
                });
    }

    @Override
    public Single<Batch> update(Principal principal, String id, BatchUpdateCommand command) {
        return batchRepository.find(id).flatMap(it -> save(principal, it, command));
    }
}
