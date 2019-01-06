package com.hengyi.japp.mes.auto.interfaces.rest;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.ProductService;
import com.hengyi.japp.mes.auto.application.command.ProductDyeingInfoUpdateCommand;
import com.hengyi.japp.mes.auto.application.command.ProductUpdateCommand;
import com.hengyi.japp.mes.auto.domain.Product;
import com.hengyi.japp.mes.auto.domain.ProductProcess;
import com.hengyi.japp.mes.auto.repository.ProductProcessRepository;
import com.hengyi.japp.mes.auto.repository.ProductRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class ProductResource {
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ProductProcessRepository productProcessRepository;

    @Inject
    private ProductResource(ProductService productService, ProductRepository productRepository, ProductProcessRepository productProcessRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.productProcessRepository = productProcessRepository;
    }

    @Path("products")
    @POST
    public Single<Product> create(Principal principal, ProductUpdateCommand command) {
        return productService.create(principal, command);
    }

    @Path("products/{id}")
    @PUT
    public Single<Product> update(Principal principal, @PathParam("id") @NotBlank String id, ProductUpdateCommand command) {
        return productService.update(principal, id, command);
    }

    @Path("products/{id}")
    @GET
    public Single<Product> get(@PathParam("id") @NotBlank String id) {
        return productRepository.find(id);
    }

    @Path("products/{id}/dyeingInfo")
    @PUT
    public Single<Product> dyeingInfo(Principal principal, @PathParam("id") @NotBlank String id, ProductDyeingInfoUpdateCommand command) {
        return productService.update(principal, id, command);
    }

    @Path("products/{id}/dyeingInfo")
    @GET
    public Single<Map> dyeingInfo(@PathParam("id") @NotBlank String id) {
        return productRepository.find(id).map(product -> {
            final Map<String, Object> result = Maps.newHashMap();
            Optional.ofNullable(product.getDyeingFormConfig())
                    .ifPresent(it -> result.put("dyeingFormConfig", it));
            result.put("dyeingExceptions", J.emptyIfNull(product.getDyeingExceptions()));
            result.put("dyeingNotes", J.emptyIfNull(product.getDyeingNotes()));
            return result;
        });
    }

    @Path("products/{id}/productProcesses")
    @GET
    public Flowable<ProductProcess> productProcesses(@PathParam("id") @NotBlank String id) {
        return productProcessRepository.listByProductId(id);
    }

    @Path("products")
    @GET
    public Flowable<Product> query() {
        return productRepository.list();
    }
}
