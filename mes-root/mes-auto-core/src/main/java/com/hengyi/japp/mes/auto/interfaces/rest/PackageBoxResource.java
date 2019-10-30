package com.hengyi.japp.mes.auto.interfaces.rest;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.PackageBoxService;
import com.hengyi.japp.mes.auto.application.command.PackageBoxAppendCommand;
import com.hengyi.japp.mes.auto.application.command.PackageBoxBatchPrintUpdateCommand;
import com.hengyi.japp.mes.auto.application.command.PackageBoxMeasureInfoUpdateCommand;
import com.hengyi.japp.mes.auto.application.event.SmallPackageBoxEvent;
import com.hengyi.japp.mes.auto.application.query.LocalDateRange;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQuery;
import com.hengyi.japp.mes.auto.application.query.PackageBoxQueryForMeasure;
import com.hengyi.japp.mes.auto.domain.PackageBox;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxType;
import com.hengyi.japp.mes.auto.repository.PackageBoxRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class PackageBoxResource {
    private final PackageBoxService packageBoxService;
    private final PackageBoxRepository packageBoxRepository;

    @Inject
    private PackageBoxResource(PackageBoxService packageBoxService, PackageBoxRepository packageBoxRepository) {
        this.packageBoxService = packageBoxService;
        this.packageBoxRepository = packageBoxRepository;
    }

    @Path("smallPackageBoxes")
    @POST
    public Flowable<PackageBox> handle(Principal principal, SmallPackageBoxEvent.BatchCommand command) {
        return packageBoxService.handle(principal, command);
    }

    @Path("smallPackageBoxes/batchIds/{batchId}")
    @GET
    public Flowable<PackageBox> handle(Principal principal, @PathParam("batchId") String smallBatchId) {
        final PackageBoxQuery query = PackageBoxQuery.builder().smallBatchId(smallBatchId).pageSize(Integer.MAX_VALUE).build();
        return packageBoxRepository.query(query).flattenAsFlowable(it -> it.getPackageBoxes());
    }

    @Path("packageBoxAppend")
    @POST
    public Single<PackageBox> handle(Principal principal, PackageBoxAppendCommand command) {
        return packageBoxService.handle(principal, command);
    }

    @Path("packageBoxes/{id}")
    @GET
    public Single<PackageBox> get(@PathParam("id") String id) {
        return packageBoxRepository.find(id);
    }

    @Path("packageBoxes/{id}/silks")
    @GET
    public Flowable<Silk> silks(@PathParam("id") String id) {
        return packageBoxRepository.find(id).flattenAsFlowable(it -> J.emptyIfNull(it.getSilks()));
    }

    @Path("packageBoxes/{id}/silkCarRecords")
    @GET
    public Flowable<SilkCarRecord> silkCarRecords(@PathParam("id") String id) {
        return packageBoxRepository.find(id).flattenAsFlowable(it -> J.emptyIfNull(it.getSilkCarRecords()));
    }

    @Path("packageBoxes/{id}/silkCarRecordsSmall")
    @GET
    public Flowable<SilkCarRecord> silkCarRecordsSmall(@PathParam("id") String id) {
        return packageBoxRepository.find(id).flattenAsFlowable(it -> J.emptyIfNull(it.getSilkCarRecordsSmall()));
    }

    @Path("packageBoxes/{id}")
    @DELETE
    public Completable delete(Principal principal, @PathParam("id") String id) {
        return packageBoxService.delete(principal, id);
    }

    @Path("packageBoxes/{id}/print")
    @PUT
    public Completable print(Principal principal, @PathParam("id") String id) {
        return packageBoxService.print(principal, id);
    }

    @Path("packageBoxBatchPrint")
    @POST
    public Completable print(Principal principal, PackageBoxBatchPrintUpdateCommand command) {
        return packageBoxService.print(principal, command);
    }

    @Path("packageBoxes/{id}/measureInfo")
    @PUT
    public Single<PackageBox> list(Principal principal, @PathParam("id") String id, PackageBoxMeasureInfoUpdateCommand command) {
        return packageBoxService.update(principal, id, command);
    }

    @Path("packageBoxes/{id}/inWarehouse")
    @DELETE
    public Completable unInWarehouse(Principal principal, @PathParam("id") String id) {
        return packageBoxService.unInWarehouse(principal, id);
    }

    /**
     * 已入库
     */
    @Path("packageBoxes")
    @GET
    public Single<PackageBoxQuery.Result> query(@QueryParam("first") @DefaultValue("0") @Min(0) int first,
                                                @QueryParam("pageSize") @DefaultValue("50") @Min(1) int pageSize,
                                                @QueryParam("workshopId") @NotBlank String workshopId,
                                                @QueryParam("startDate") @NotBlank String startDate,
                                                @QueryParam("endDate") @NotBlank String endDate,
                                                @QueryParam("packageBoxType") String typeString,
                                                @QueryParam("packageBoxCode") String packageBoxCode,
                                                @QueryParam("budatClassId") String budatClassId,
                                                @QueryParam("gradeId") String gradeId,
                                                @QueryParam("batchId") String batchId,
                                                @QueryParam("productId") String productId) {
        final Set<String> budatClassIds = J.nonBlank(budatClassId) ? Sets.newHashSet(budatClassId) : Collections.EMPTY_SET;
        final LocalDate startLd = Optional.ofNullable(startDate)
                .filter(J::nonBlank)
                .map(LocalDate::parse)
                .orElse(LocalDate.now());
        final LocalDate endLd = Optional.ofNullable(endDate)
                .filter(J::nonBlank)
                .map(LocalDate::parse)
                .orElse(LocalDate.now());
        final PackageBoxType type = Optional.ofNullable(typeString)
                .filter(J::nonBlank)
                .map(PackageBoxType::valueOf)
                .orElse(null);
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .first(first)
                .pageSize(pageSize)
                .budatRange(new LocalDateRange(startLd, endLd.plusDays(1)))
                .type(type)
                .packageBoxCode(packageBoxCode)
                .workshopId(workshopId)
                .gradeId(gradeId)
                .batchId(batchId)
                .productId(productId)
                .budatClassIds(budatClassIds)
                .build();
        return packageBoxRepository.query(packageBoxQuery);
    }

    /**
     * 待入库
     */
    @Path("measurePackageBoxes")
    @GET
    public Single<PackageBoxQueryForMeasure.Result> queryPrepare(@QueryParam("first") @DefaultValue("0") @Min(0) int first,
                                                                 @QueryParam("pageSize") @DefaultValue("50") @Min(1) int pageSize,
                                                                 @QueryParam("workshopId") @NotBlank String workshopId,
                                                                 @QueryParam("startDate") @NotBlank String startDate,
                                                                 @QueryParam("endDate") @NotBlank String endDate,
                                                                 @QueryParam("packageBoxType") String typeString,
                                                                 @QueryParam("packageBoxCode") String packageBoxCode,
                                                                 @QueryParam("netWeight") double netWeight,
                                                                 @QueryParam("gradeId") String gradeId,
                                                                 @QueryParam("batchId") String batchId,
                                                                 @QueryParam("productId") String productId,
                                                                 @QueryParam("automaticPackeLine") String automaticPackeLine,
                                                                 @QueryParam("creatorId") String creatorId) {
        final LocalDate startLd = Optional.ofNullable(startDate)
                .filter(J::nonBlank)
                .map(LocalDate::parse)
                .orElse(LocalDate.now());
        final LocalDate endLd = Optional.ofNullable(endDate)
                .filter(J::nonBlank)
                .map(LocalDate::parse)
                .orElse(LocalDate.now());
        final PackageBoxType type = Optional.ofNullable(typeString)
                .filter(J::nonBlank)
                .map(PackageBoxType::valueOf)
                .orElse(null);
        final PackageBoxQueryForMeasure packageBoxQuery = PackageBoxQueryForMeasure.builder()
                .first(first)
                .pageSize(pageSize)
                .createDateTimeRange(new LocalDateRange(startLd, endLd.plusDays(1)))
                .type(type)
                .packageBoxCode(packageBoxCode)
                .workshopId(workshopId)
                .gradeId(gradeId)
                .batchId(batchId)
                .automaticPackeLine(automaticPackeLine)
                .productId(productId)
                .creatorId(creatorId)
                .netWeight(netWeight)
                .build();
        return packageBoxRepository.query(packageBoxQuery);
    }

    /**
     * 打包工，当天，打包列表
     */
    @Path("currentSelPackageBoxes")
    @GET
    public Single<Map<String, Object>> queryPrepare(Principal principal,
                                                    @QueryParam("first") @DefaultValue("0") @Min(0) int first,
                                                    @QueryParam("pageSize") @DefaultValue("50") @Min(1) int pageSize) {
        final Map<String, Object> result = Maps.newConcurrentMap();
        final LocalDateRange ldRange = new LocalDateRange(LocalDate.now(), LocalDate.now().plusDays(1));
        final PackageBoxQueryForMeasure packageBoxQueryForMeasure = PackageBoxQueryForMeasure.builder()
                .first(first)
                .pageSize(pageSize)
                .createDateTimeRange(ldRange)
                .type(PackageBoxType.MANUAL)
                .creatorId(principal.getName())
                .build();
        final Completable measureResult$ = packageBoxRepository.query(packageBoxQueryForMeasure)
                .flatMapCompletable(it -> {
                    result.put("measureResult", it);
                    return Completable.complete();
                });
        final PackageBoxQuery packageBoxQuery = PackageBoxQuery.builder()
                .first(first)
                .pageSize(pageSize)
                .budatRange(ldRange)
                .type(PackageBoxType.MANUAL)
                .creatorId(principal.getName())
                .build();
        final Completable warehouseResult$ = packageBoxRepository.query(packageBoxQuery)
                .flatMapCompletable(it -> {
                    result.put("warehouseResult", it);
                    return Completable.complete();
                });

        return Completable.mergeArray(measureResult$, warehouseResult$).toSingle(() -> result);
    }

}
