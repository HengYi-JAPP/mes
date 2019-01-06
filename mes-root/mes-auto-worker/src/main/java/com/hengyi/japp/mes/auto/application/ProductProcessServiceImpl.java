package com.hengyi.japp.mes.auto.application;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.ProductProcessUpdateCommand;
import com.hengyi.japp.mes.auto.domain.ProductProcess;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.security.Principal;
import java.util.Optional;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class ProductProcessServiceImpl implements ProductProcessService {
    private final ProductRepository productRepository;
    private final ProductProcessRepository productProcessRepository;
    private final SilkExceptionRepository silkExceptionRepository;
    private final SilkNoteRepository silkNoteRepository;
    private final FormConfigRepository formConfigRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private ProductProcessServiceImpl(ProductRepository productRepository, ProductProcessRepository productProcessRepository, SilkExceptionRepository silkExceptionRepository, SilkNoteRepository silkNoteRepository, FormConfigRepository formConfigRepository, OperatorRepository operatorRepository) {
        this.productRepository = productRepository;
        this.productProcessRepository = productProcessRepository;
        this.silkExceptionRepository = silkExceptionRepository;
        this.silkNoteRepository = silkNoteRepository;
        this.formConfigRepository = formConfigRepository;
        this.operatorRepository = operatorRepository;
    }

    @Override
    public Single<ProductProcess> create(Principal principal, ProductProcessUpdateCommand command) {
        return productProcessRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<ProductProcess> save(Principal principal, ProductProcess productProcess, ProductProcessUpdateCommand command) {
        productProcess.setName(command.getName());
        productProcess.setSortBy(command.getSortBy());
        productProcess.setRelateRoles(command.getRelateRoles());
        productProcess.setMustProcess(command.isMustProcess());

        return Flowable.fromIterable(CollectionUtils.emptyIfNull(command.getNotes()))
                .map(EntityDTO::getId)
                .flatMapSingle(silkNoteRepository::find).toList()
                .flatMap(notes -> {
                    productProcess.setNotes(notes);
                    return Flowable.fromIterable(command.getExceptions())
                            .map(EntityDTO::getId)
                            .flatMapSingle(silkExceptionRepository::find).toList();
                })
                .flatMap(exceptions -> {
                    productProcess.setExceptions(exceptions);
                    return productRepository.find(command.getProduct().getId());
                })
                .flatMap(product -> {
                    productProcess.setProduct(product);

                    final Completable formConfigSet$;
                    final String formConfigId = Optional.ofNullable(command.getFormConfig())
                            .map(EntityDTO::getId)
                            .orElse(null);
                    if (StringUtils.isBlank(formConfigId)) {
                        productProcess.setFormConfig(null);
                        formConfigSet$ = Completable.complete();
                    } else {
                        formConfigSet$ = formConfigRepository.find(formConfigId)
                                .doAfterSuccess(productProcess::setFormConfig)
                                .ignoreElement();
                    }
                    return formConfigSet$.andThen(operatorRepository.find(principal));
                })
                .flatMap(operator -> {
                    productProcess.log(operator);
                    return productProcessRepository.save(productProcess);
                });
    }

    @Override
    public Single<ProductProcess> update(Principal principal, String id, ProductProcessUpdateCommand command) {
        return productProcessRepository.find(id).flatMap(product -> save(principal, product, command));
    }

}
