package com.hengyi.japp.mes.auto.interfaces.rest;

import com.github.ixtf.japp.core.J;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.query.PackageBoxFlipQuery;
import com.hengyi.japp.mes.auto.domain.data.PackageBoxFlipType;
import com.hengyi.japp.mes.auto.repository.PackageBoxFlipRepository;
import io.reactivex.Single;

import javax.validation.constraints.Min;
import javax.ws.rs.*;
import java.time.LocalDate;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class PackageBoxFlipResource {
    private final PackageBoxFlipRepository packageBoxFlipRepository;

    @Inject
    private PackageBoxFlipResource(PackageBoxFlipRepository packageBoxFlipRepository) {
        this.packageBoxFlipRepository = packageBoxFlipRepository;
    }

    @Path("packageBoxFlips")
    @GET
    public Single<PackageBoxFlipQuery.Result> query(@QueryParam("first") @DefaultValue("0") @Min(0) int first,
                                                    @QueryParam("pageSize") @DefaultValue("50") @Min(1) int pageSize,
                                                    @QueryParam("packageBoxFlipType") String packageBoxFlipTypeString,
                                                    @QueryParam("startDate") String startDate,
                                                    @QueryParam("endDate") String endDate,
                                                    @QueryParam("packageBoxId") String packageBoxId,
                                                    @QueryParam("gradeId") String gradeId,
                                                    @QueryParam("batchId") String batchId,
                                                    @QueryParam("workshopId") String workshopId) {
        final LocalDate startLd = Optional.ofNullable(startDate)
                .filter(J::nonBlank)
                .map(LocalDate::parse)
                .orElse(LocalDate.now());
        final LocalDate endLd = Optional.ofNullable(endDate)
                .filter(J::nonBlank)
                .map(LocalDate::parse)
                .orElse(LocalDate.now());
        final PackageBoxFlipType packageBoxFlipType = Optional.ofNullable(packageBoxFlipTypeString)
                .filter(J::nonBlank)
                .map(PackageBoxFlipType::valueOf)
                .orElse(null);
        final PackageBoxFlipQuery packageBoxFlipQuery = PackageBoxFlipQuery.builder()
                .first(first)
                .pageSize(pageSize)
                .startLd(startLd)
                .endLd(endLd.plusDays(1))
                .packageBoxId(packageBoxId)
                .gradeId(gradeId)
                .batchId(batchId)
                .workshopId(workshopId)
                .packageBoxFlipType(packageBoxFlipType)
                .build();
        return packageBoxFlipRepository.query(packageBoxFlipQuery);
    }
}
