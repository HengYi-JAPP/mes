package com.hengyi.japp.mes.auto.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.application.command.SilkCarRuntimeDeleteCommand;
import com.hengyi.japp.mes.auto.application.command.SilkCarRuntimeFlipCommand;
import com.hengyi.japp.mes.auto.application.event.*;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.dto.SilkCarRecordDTO;
import com.hengyi.japp.mes.auto.exception.MultiBatchException;
import com.hengyi.japp.mes.auto.exception.SilkCarStatusException;
import io.reactivex.Completable;
import io.reactivex.Single;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.security.Principal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jzb 2018-06-22
 */
public interface SilkCarRuntimeService {
    /**
     * 检查混批
     */
    static Batch checkAndGetBatch(Collection<SilkRuntime> silkRuntimes) throws MultiBatchException {
        final Set<Batch> batches = J.emptyIfNull(silkRuntimes).stream()
                .map(SilkRuntime::getSilk)
                .map(Silk::getBatch)
                .collect(Collectors.toSet());
        if (batches.size() > 1) {
            throw new MultiBatchException();
        }
        return IterableUtils.get(batches, 0);
    }

    /**
     * 检查是否这辆车上的丝锭
     */
    static Collection<SilkRuntime> checkAndGetSilkRuntimes(SilkCarRuntime silkCarRuntime, Collection<SilkRuntime.DTO> dtos) throws Exception {
        final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
        final SilkCar silkCar = silkCarRecord.getSilkCar();
        Collection<SilkRuntime> result = Lists.newArrayList();
        final Map<Triple<SilkCarSideType, Integer, Integer>, SilkRuntime> map = silkCarRuntime.getSilkRuntimes()
                .stream()
                .collect(Collectors.toMap(it -> Triple.of(it.getSideType(), it.getRow(), it.getCol()), Function.identity()));
        for (SilkRuntime.DTO dto : dtos) {
            final Triple<SilkCarSideType, Integer, Integer> triple = Triple.of(dto.getSideType(), dto.getRow(), dto.getCol());
            result.add(map.get(triple));
            final String silkId = Optional.ofNullable(map.get(triple))
                    .map(SilkRuntime::getSilk)
                    .map(Silk::getId)
                    .orElse(null);
            if (!Objects.equals(silkId, dto.getSilk().getId())) {
                throw new SilkCarStatusException(silkCar);
            }
        }
        return result;
    }

    Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeInitEvent.AutoDoffingAdaptCheckSilksCommand command);

    Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.AutoDoffingAdaptCommand command);

    Single<SilkCarRuntime> doffing(SilkCarRuntimeInitEvent event, DoffingType doffingType);

    Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeInitEvent.ManualDoffingAdaptCheckSilksCommand command);

    Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.ManualDoffingCommand command);

    Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeInitEvent.DyeingSampleDoffingCheckSilksCommand command);

    Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.DyeingSampleDoffingCommand command);

    Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeInitEvent.PhysicalInfoDoffingCheckSilksCommand command);

    Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.PhysicalInfoDoffingCommand command);

    Single<List<CheckSilkDTO>> handle(Principal principal, SilkCarRuntimeAppendEvent.CheckSilksCommand command);

    Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeAppendEvent.Command command);

    Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.CarpoolCommand command);

    Completable handle(SilkCarRuntime silkCarRuntime, SilkCarRuntimeGradeEvent event);

    Completable handle(SilkCarRuntime silkCarRuntime, SilkCarRuntimeGradeSubmitEvent event);

    Completable handle(SilkCarRuntime silkCarRuntime, ProductProcessSubmitEvent event);

    Completable handle(SilkCarRuntime silkCarRuntime, ExceptionCleanEvent event);

    Completable handle(SilkCarRuntime silkCarRuntime, DyeingSampleSubmitEvent event);

    Completable handle(SilkCarRuntime silkCarRuntime, SilkRuntimeDetachEvent event);

    Completable handle(SilkCarRuntime silkCarRuntime, TemporaryBoxEvent event);

    Completable handle(DyeingPrepareEvent event, SilkCarRuntime silkCarRuntime, Collection<SilkRuntime> silkRuntimes);

    Completable handle(DyeingPrepareEvent event, SilkCarRuntime silkCarRuntime1, Collection<SilkRuntime> silkRuntimes1, SilkCarRuntime silkCarRuntime2, Collection<SilkRuntime> silkRuntimes2);

    Completable handle(Principal principal, SilkNoteFeedbackEvent.Command command);

    Single<JsonNode> physicalInfo(String code);

    Single<SilkCarRuntime> find(SilkCarRecordDTO dto);

    Completable undoEventSource(Principal principal, String code, String eventSourceId);

    Completable delete(Principal principal, SilkCarRuntimeDeleteCommand command);

    Completable flip(Principal principal, SilkCarRuntimeFlipCommand command);
}
