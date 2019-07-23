package com.hengyi.japp.mes.auto.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.PackageBoxService;
import com.hengyi.japp.mes.auto.application.SilkCarRuntimeService;
import com.hengyi.japp.mes.auto.application.command.SilkCarRuntimeDeleteCommand;
import com.hengyi.japp.mes.auto.application.command.SilkCarRuntimeFlipCommand;
import com.hengyi.japp.mes.auto.application.event.*;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.exception.SilkCarStatusException;
import com.hengyi.japp.mes.auto.repository.SilkCarRuntimeRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.security.Principal;
import java.util.Collection;
import java.util.List;

import static com.hengyi.japp.mes.auto.application.SilkCarRuntimeService.checkAndGetSilkRuntimes;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2018-11-02
 */
@Singleton
@Path("api")
@Produces(APPLICATION_JSON)
public class SilkCarRuntimeResource {
    private final SilkCarRuntimeService silkCarRuntimeService;
    private final SilkCarRuntimeRepository silkCarRuntimeRepository;
    private final PackageBoxService packageBoxService;

    @Inject
    private SilkCarRuntimeResource(SilkCarRuntimeService silkCarRuntimeService, PackageBoxService packageBoxService, SilkCarRuntimeRepository silkCarRuntimeRepository) {
        this.silkCarRuntimeService = silkCarRuntimeService;
        this.packageBoxService = packageBoxService;
        this.silkCarRuntimeRepository = silkCarRuntimeRepository;
    }

    @Path("silkCarRuntimes/{code}")
    @GET
    public Single<SilkCarRuntime> get(@PathParam("code") @NotBlank String code) {
        return silkCarRuntimeRepository.findByCode(code).toSingle(new SilkCarRuntime());
    }

    @Path("silkCarRuntimes/{code}/physicalInfo")
    @GET
    public Single<JsonNode> physicalInfo(@PathParam("code") @NotBlank String code) {
        return silkCarRuntimeService.physicalInfo(code);
    }

    @Path("BigSilkCarDoffingEvents")
    @POST
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.BigSilkCarDoffingCommand command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("BigSilkCarDoffingAppendEvents")
    @POST
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeAppendEvent.BigSilkCarDoffingAppendCommand command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("BigSilkCarSilkChangeEvents")
    @POST
    public Completable handle(Principal principal, BigSilkCarSilkChangeEvent.Command command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("AutoDoffingAdaptCheckSilks")
    @POST
    public Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeInitEvent.AutoDoffingAdaptCheckSilksCommand command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("AutoDoffingAdaptEvents")
    @POST
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.AutoDoffingAdaptCommand command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("ManualDoffingCheckSilks")
    @POST
    public Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeInitEvent.ManualDoffingAdaptCheckSilksCommand command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("ManualDoffingEvents")
    @POST
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.ManualDoffingCommand command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("DyeingSampleDoffingCheckSilks")
    @POST
    public Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeInitEvent.DyeingSampleDoffingCheckSilksCommand command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("DyeingSampleDoffingEvents")
    @POST
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.DyeingSampleDoffingCommand command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("PhysicalInfoDoffingCheckSilks")
    @POST
    public Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeInitEvent.PhysicalInfoDoffingCheckSilksCommand command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("PhysicalInfoDoffingEvents")
    @POST
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.PhysicalInfoDoffingCommand command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("AppendDoffingCheckSilks")
    @POST
    public Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeAppendEvent.CheckSilksCommand command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("AppendDoffingEvents")
    @POST
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeAppendEvent.Command command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("CarpoolEvents")
    @POST
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.CarpoolCommand command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("SilkCarRuntimeGradeEvents")
    @POST
    public Completable handle(Principal principal, SilkCarRuntimeGradeEvent.Command command) {
        return command.toEvent(principal).flatMapCompletable(event -> silkCarRuntimeService.find(command.getSilkCarRecord()).flatMapCompletable(silkCarRuntime ->
                silkCarRuntimeService.handle(silkCarRuntime, event)
        ));
    }

    @Path("DyeingSampleSubmitEvents")
    @POST
    public Completable handle(Principal principal, DyeingSampleSubmitEvent.Command command) {
        return command.toEvent(principal).flatMapCompletable(event -> silkCarRuntimeService.find(command.getSilkCarRecord()).flatMapCompletable(silkCarRuntime ->
                silkCarRuntimeService.handle(silkCarRuntime, event)
        ));
    }

    @Path("SilkRuntimeDetachEvents")
    @POST
    public Completable handle(Principal principal, SilkRuntimeDetachEvent.Command command) {
        return command.toEvent(principal).flatMapCompletable(event -> silkCarRuntimeService.find(command.getSilkCarRecord()).flatMapCompletable(silkCarRuntime ->
                silkCarRuntimeService.handle(silkCarRuntime, event)
        ));
    }

    @Path("ProductProcessSubmitEvents")
    @POST
    public Completable handle(Principal principal, ProductProcessSubmitEvent.Command command) {
        return command.toEvent(principal).flatMapCompletable(event -> silkCarRuntimeService.find(command.getSilkCarRecord()).flatMapCompletable(silkCarRuntime ->
                silkCarRuntimeService.handle(silkCarRuntime, event)
        ));
    }

    @Path("ExceptionCleanEvents")
    @POST
    public Completable get(Principal principal, ExceptionCleanEvent.Command command) {
        return command.toEvent(principal).flatMapCompletable(event -> silkCarRuntimeService.find(command.getSilkCarRecord()).flatMapCompletable(silkCarRuntime ->
                silkCarRuntimeService.handle(silkCarRuntime, event)
        ));
    }

    @Path("SilkCarRuntimeWeightEvent")
    @POST
    public Completable handle(Principal principal, SilkCarRuntimeWeightEvent.Command command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("SilkCarRuntimeGradeSubmitEvents")
    @POST
    public Completable handle(Principal principal, SilkCarRuntimeGradeSubmitEvent.Command command) {
        return command.toEvent(principal).flatMapCompletable(event -> silkCarRuntimeService.find(command.getSilkCarRecord()).flatMapCompletable(silkCarRuntime ->
                silkCarRuntimeService.handle(silkCarRuntime, event)
        ));
    }

    @Path("DyeingPrepareFirstSubmitEvents")
    @POST
    public Completable handle(Principal principal, DyeingPrepareEvent.FirstSubmitCommand.Batch batch) {
        return Flowable.fromIterable(batch.getCommands()).flatMapCompletable(command -> handle(principal, command));
    }

    private Completable handle(Principal principal, DyeingPrepareEvent.FirstSubmitCommand command) {
        return command.toEvent(principal).flatMapCompletable(event -> silkCarRuntimeService.find(command.getSilkCarRecord()).flatMapCompletable(silkCarRuntime -> {
            final Collection<SilkRuntime> silkRuntimes = checkAndGetSilkRuntimes(silkCarRuntime, command.getSilkRuntimes());
            final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
            final SilkCar silkCar = silkCarRecord.getSilkCar();
            if (silkCarRecord.getCarpoolOperator() != null) {
                throw new SilkCarStatusException(silkCar);
            }
            return silkCarRuntimeService.handle(event, silkCarRuntime, silkRuntimes);
        }));
    }

    @Path("DyeingPrepareCrossSpindleSubmitEvents")
    @POST
    public Completable handle(Principal principal, DyeingPrepareEvent.CrossSpindleSubmitCommand.Batch batch) {
        return Flowable.fromIterable(batch.getCommands()).flatMapCompletable(command -> handle(principal, command));
    }

    private Completable handle(Principal principal, DyeingPrepareEvent.CrossSpindleSubmitCommand command) {
        return command.toEvent(principal).flatMapCompletable(event -> silkCarRuntimeService.find(command.getSilkCarRecord()).flatMapCompletable(silkCarRuntime -> {
            final Collection<SilkRuntime> silkRuntimes = checkAndGetSilkRuntimes(silkCarRuntime, command.getSilkRuntimes());
            return silkCarRuntimeService.handle(event, silkCarRuntime, silkRuntimes);
        }));
    }

    @Path("DyeingPrepareCrossLineMachineSubmitEvents")
    @POST
    public Completable handle(Principal principal, DyeingPrepareEvent.CrossLineMachineSubmitCommand command) {
        final Single<SilkCarRuntime> silkCarRuntime1$ = silkCarRuntimeService.find(command.getSilkCarRecord1());
        final Single<SilkCarRuntime> silkCarRuntime2$ = silkCarRuntimeService.find(command.getSilkCarRecord2());
        return command.toEvent(principal).flatMapCompletable(event -> silkCarRuntime1$.flatMapCompletable(silkCarRuntime1 -> silkCarRuntime2$.flatMapCompletable(silkCarRuntime2 -> {
            final Collection<SilkRuntime> silkRuntimes1 = checkAndGetSilkRuntimes(silkCarRuntime1, command.getSilkRuntimes1());
            final Collection<SilkRuntime> silkRuntimes2 = checkAndGetSilkRuntimes(silkCarRuntime2, command.getSilkRuntimes2());
            return silkCarRuntimeService.handle(event, silkCarRuntime1, silkRuntimes1, silkCarRuntime2, silkRuntimes2);
        })));
    }

    @Path("DyeingPrepareMultiSubmitEvents")
    @POST
    public Completable handle(Principal principal, DyeingPrepareEvent.MultiSubmitCommand command) {
        return command.toEvent(principal).flatMapCompletable(event -> silkCarRuntimeService.find(command.getSilkCarRecord()).flatMapCompletable(silkCarRuntime -> {
            final Collection<SilkRuntime> silkRuntimes = checkAndGetSilkRuntimes(silkCarRuntime, command.getSilkRuntimes());
            final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
            final SilkCar silkCar = silkCarRecord.getSilkCar();
            if (silkCarRecord.getDoffingType() != null) {
                throw new SilkCarStatusException(silkCar);
            }
            return silkCarRuntimeService.handle(event, silkCarRuntime, silkRuntimes);
        }));
    }

    @Path("TemporaryBoxIncrementEvents")
    @POST
    public Completable handle(Principal principal, TemporaryBoxEvent.FromSilkCarRuntimeCommand command) {
        return command.toEvent(principal).flatMapCompletable(event -> silkCarRuntimeService.find(command.getSilkCarRecord()).flatMapCompletable(silkCarRuntime ->
                silkCarRuntimeService.handle(silkCarRuntime, event)
        ));
    }

//    @Path("ManualPackageBoxEvents")
//    @POST
//    public Single<PackageBox> handle(Principal principal, PackageBoxEvent.ManualCommand command) {
//        return packageBoxService.handle(principal, command);
//    }

    @Path("ManualPackageBoxEventsSimple")
    @POST
    public Single<PackageBox> handle(Principal principal, PackageBoxEvent.ManualCommandSimple command) {
        return packageBoxService.handle(principal, command);
    }

    @Deprecated
    @Path("TemporaryBoxPackageBoxEvents")
    @POST
    public Single<PackageBox> get(Principal principal, PackageBoxEvent.TemporaryBoxCommand command) {
        return packageBoxService.handle(principal, command);
    }

    @Path("SilkNoteFeedbackEvents")
    @POST
    public Completable handle(Principal principal, SilkNoteFeedbackEvent.Command command) {
        return silkCarRuntimeService.handle(principal, command);
    }

    @Path("silkCarRuntimes/{code}/eventSources/{eventSourceId}")
    @DELETE
    public Completable undoEventSource(Principal principal, @PathParam("code") @NotBlank String code, @PathParam("eventSourceId") @NotBlank String eventSourceId) {
        return silkCarRuntimeService.undoEventSource(principal, code, eventSourceId);
    }

    @Path("SilkCarRuntimeDeleteEvents")
    @POST
    public Completable delete(Principal principal, SilkCarRuntimeDeleteCommand command) {
        return silkCarRuntimeService.delete(principal, command);
    }

    @Path("SilkCarRuntimeFlipEvents")
    @POST
    public Completable flip(Principal principal, SilkCarRuntimeFlipCommand command) {
        return silkCarRuntimeService.flip(principal, command);
    }

}
