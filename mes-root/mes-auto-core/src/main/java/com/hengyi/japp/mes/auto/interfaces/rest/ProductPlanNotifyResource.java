package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.ProductPlanNotifyService;
import com.hengyi.japp.mes.auto.application.command.ProductPlanNotifyExeCommand;
import com.hengyi.japp.mes.auto.application.command.ProductPlanNotifyUpdateCommand;
import com.hengyi.japp.mes.auto.application.query.ProductPlanNotifyQuery;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.ProductPlanNotify;
import com.hengyi.japp.mes.auto.repository.ProductPlanNotifyRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.security.Principal;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class ProductPlanNotifyResource {
    private final ProductPlanNotifyService productPlanNotifyService;
    private final ProductPlanNotifyRepository productPlanNotifyRepository;

    @Inject
    private ProductPlanNotifyResource(ProductPlanNotifyService productPlanNotifyService, ProductPlanNotifyRepository productPlanNotifyRepository) {
        this.productPlanNotifyService = productPlanNotifyService;
        this.productPlanNotifyRepository = productPlanNotifyRepository;
    }

    @Path("productPlanNotifies")
    @POST
    public Single<ProductPlanNotify> create(Principal principal, ProductPlanNotifyUpdateCommand command) {
        return productPlanNotifyService.create(principal, command);
    }

    @Path("productPlanNotifies/{id}")
    @PUT
    public Single<ProductPlanNotify> update(Principal principal, @PathParam("id") @NotBlank String id, ProductPlanNotifyUpdateCommand command) {
        return productPlanNotifyService.update(principal, id, command);
    }

    @Path("productPlanNotifies/{id}")
    @GET
    public Single<ProductPlanNotify> get(@PathParam("id") @NotBlank String id) {
        return productPlanNotifyRepository.find(id);
    }

//    @Path("productPlanNotifies/{id}")
//    @DELETE
//    @Produces(APPLICATION_JSON)
//    public Single<Grade> delete(Principal principal, @PathParam("id") @NotBlank String id) {
//        return productPlanNotifyService.find(id);
//    }

    @Path("productPlanNotifies/{id}/exe")
    @POST
    public Completable exe(Principal principal, @PathParam("id") @NotBlank String id, ProductPlanNotifyExeCommand command) {
        return productPlanNotifyService.exe(principal, id, command);
    }

    @Path("productPlanNotifies/{id}/unExe")
    @DELETE
    public Completable unExe(Principal principal, @PathParam("id") @NotBlank String id, ProductPlanNotifyExeCommand command) {
        return productPlanNotifyService.unExe(principal, id, command);
    }

    @Path("productPlanNotifies/{id}/batchExe")
    @POST
    public Completable get(Principal principal, @PathParam("id") @NotBlank String id, ProductPlanNotifyExeCommand.Batch commands) {
        return Flowable.fromIterable(commands.getLineMachines())
                .flatMapCompletable(it -> {
                    final ProductPlanNotifyExeCommand command = new ProductPlanNotifyExeCommand();
                    command.setLineMachine(it);
                    return productPlanNotifyService.exe(principal, id, command);
                });
    }

    @Path("productPlanNotifies/{id}/exeInfo")
    @GET
    public Single<Map> exeInfo(@PathParam("id") @NotBlank String id) {
        final Map<String, Object> result = Maps.newHashMap();
        return productPlanNotifyRepository.find(id)
                .flatMap(productPlanNotify -> {
                    result.put("productPlanNotify", productPlanNotify);
                    return Flowable.fromIterable(CollectionUtils.emptyIfNull(productPlanNotify.getLineMachines()))
                            .filter(lineMachine -> lineMachine.getProductPlan() != null)
                            .map(LineMachine::getProductPlan)
                            .toList();
                })
                .map(lineMachineProductPlans -> {
                    result.put("lineMachineProductPlans", lineMachineProductPlans);
                    return result;
                });
    }

    @Path("productPlanNotifies/{id}/finish")
    @PUT
    public Completable finish(Principal principal, @PathParam("id") @NotBlank String id) {
        return productPlanNotifyService.finish(principal, id);
    }

    @Path("productPlanNotifies/{id}/finish")
    @DELETE
    public Completable unFinish(Principal principal, @PathParam("id") @NotBlank String id) {
        return productPlanNotifyService.unFinish(principal, id);
    }

    @Path("productPlanNotifies")
    @GET
    public Single<ProductPlanNotifyQuery.Result> query(@QueryParam("first") @DefaultValue("0") @Min(0) int first,
                                                       @QueryParam("pageSize") @DefaultValue("50") @Min(1) int pageSize,
                                                       @QueryParam("q") String q) {
        final ProductPlanNotifyQuery productPlanNotifyQuery = ProductPlanNotifyQuery.builder()
                .first(first)
                .pageSize(pageSize)
                .q(q)
                .build();
        return productPlanNotifyRepository.query(productPlanNotifyQuery);
    }
}
