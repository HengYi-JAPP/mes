package com.hengyi.japp.mes.auto.interfaces.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.repository.*;
import io.reactivex.Flowable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api/autoComplete")
@Produces(APPLICATION_JSON)
public class AutoCompleteResource {
    private final LineRepository lineRepository;
    private final SilkCarRepository silkCarRepository;
    private final FormConfigRepository formConfigRepository;
    private final PermissionRepository permissionRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private AutoCompleteResource(LineRepository lineRepository, SilkCarRepository silkCarRepository, FormConfigRepository formConfigRepository, PermissionRepository permissionRepository, OperatorRepository operatorRepository) {
        this.lineRepository = lineRepository;
        this.silkCarRepository = silkCarRepository;
        this.formConfigRepository = formConfigRepository;
        this.permissionRepository = permissionRepository;
        this.operatorRepository = operatorRepository;
    }

    @Path("line")
    @GET
    public Flowable<Line> line(@QueryParam("q") String q) {
        return lineRepository.autoComplete(q);
    }

    @Path("silkCar")
    @GET
    public Flowable<SilkCar> silkCar(@QueryParam("q") String q) {
        return silkCarRepository.autoComplete(q);
    }

    @Path("formConfig")
    @GET
    public Flowable<FormConfig> formConfig(@QueryParam("q") String q) {
        return formConfigRepository.autoComplete(q);
    }

    @Path("permission")
    @GET
    public Flowable<Permission> permission(@QueryParam("q") String q) {
        return permissionRepository.autoComplete(q);
    }

    @Path("operator")
    @GET
    public Flowable<Operator> operator(@QueryParam("q") String q) {
        return operatorRepository.autoComplete(q);
    }

}
