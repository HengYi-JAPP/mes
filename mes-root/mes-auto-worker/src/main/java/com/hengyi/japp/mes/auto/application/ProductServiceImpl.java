package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.command.ProductDyeingInfoUpdateCommand;
import com.hengyi.japp.mes.auto.application.command.ProductUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Product;
import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.Optional;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final OperatorRepository operatorRepository;
    private final FormConfigRepository formConfigRepository;
    private final SilkExceptionRepository silkExceptionRepository;
    private final SilkNoteRepository silkNoteRepository;

    @Inject
    private ProductServiceImpl(ProductRepository productRepository, OperatorRepository operatorRepository, FormConfigRepository formConfigRepository, SilkExceptionRepository silkExceptionRepository, SilkNoteRepository silkNoteRepository) {
        this.productRepository = productRepository;
        this.operatorRepository = operatorRepository;
        this.formConfigRepository = formConfigRepository;
        this.silkExceptionRepository = silkExceptionRepository;
        this.silkNoteRepository = silkNoteRepository;
    }

    @Override
    public Single<Product> create(Principal principal, ProductUpdateCommand command) {
        return productRepository.create().flatMap(it -> save(principal, it, command));
    }

    private Single<Product> save(Principal principal, Product product, ProductUpdateCommand command) {
        product.setName(command.getName());
        product.setCode(command.getCode());
        return operatorRepository.find(principal)
                .flatMap(operator -> {
                    product.log(operator);
                    return productRepository.save(product);
                });
    }

    @Override
    public Single<Product> update(Principal principal, String id, ProductUpdateCommand command) {
        return productRepository.find(id).flatMap(it -> save(principal, it, command));
    }

    @Override
    public Single<Product> update(Principal principal, String id, ProductDyeingInfoUpdateCommand command) {
        return productRepository.find(id).flatMap(product -> {
            final Completable setDyeingExceptions$ = Flowable.fromIterable(J.emptyIfNull(command.getDyeingExceptions()))
                    .map(EntityDTO::getId)
                    .flatMapSingle(silkExceptionRepository::find).toList()
                    .doOnSuccess(product::setDyeingExceptions)
                    .ignoreElement();
            final Completable setDyeingNotes$ = Flowable.fromIterable(J.emptyIfNull(command.getDyeingNotes()))
                    .map(EntityDTO::getId)
                    .flatMapSingle(silkNoteRepository::find).toList()
                    .doOnSuccess(product::setDyeingNotes)
                    .ignoreElement();
            final Completable setDyeingFormConfig$ = Optional.ofNullable(command.getDyeingFormConfig())
                    .map(EntityDTO::getId)
                    .map(formConfigRepository::find)
                    .map(it -> it.doOnSuccess(product::setDyeingFormConfig))
                    .map(Single::ignoreElement)
                    .orElseGet(() -> {
                        product.setDyeingFormConfig(null);
                        return Completable.complete();
                    });
            return Completable.mergeArray(setDyeingExceptions$, setDyeingNotes$, setDyeingFormConfig$)
                    .andThen(operatorRepository.find(principal))
                    .flatMap(operator -> {
                        product.log(operator);
                        return productRepository.save(product);
                    });
        });
    }

}
