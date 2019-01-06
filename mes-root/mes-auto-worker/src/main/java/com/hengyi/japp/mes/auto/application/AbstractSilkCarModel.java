package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.domain.Batch;
import com.hengyi.japp.mes.auto.domain.SilkBarcode;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import com.hengyi.japp.mes.auto.domain.data.SilkCarType;
import com.hengyi.japp.mes.auto.domain.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.exception.BatchChangedException;
import com.hengyi.japp.mes.auto.exception.DoffingCapacityException;
import com.hengyi.japp.mes.auto.exception.DoffingTagException;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jzb 2018-11-15
 */
@Data
public abstract class AbstractSilkCarModel implements SilkCarModel {
    @EqualsAndHashCode.Include
    protected final SilkCar silkCar;
    protected final int silkCarCapacity;

    protected AbstractSilkCarModel(SilkCar silkCar) {
        this.silkCar = silkCar;
        int capacity = silkCar.getRow() * silkCar.getCol() * 2;
        this.silkCarCapacity = silkCar.getType() == SilkCarType.BIG_SILK_CAR ? capacity * 2 : capacity;
    }

    protected Single<List<SilkRuntime>> addAll(List<Single<SilkRuntime>> silkRuntime$List) {
        return Single.fromCallable(() -> {
            final ImmutableList.Builder<SilkRuntime> builder = ImmutableList.builder();
            final List<SilkCarPosition> orderedSilkPositions = getOrderedSilkPositions();
            if (silkRuntime$List.size() > silkCarCapacity) {
                throw new DoffingCapacityException();
            }

            final List<Completable> completables = Lists.newArrayList();
            int posIndex = 0;
            for (Single<SilkRuntime> silkRuntime$ : silkRuntime$List) {
                final SilkCarPosition silkPosition = orderedSilkPositions.get(posIndex++);
                final Completable completable = silkRuntime$.map(silkRuntime -> {
                    silkRuntime.setSideType(silkPosition.getSideType());
                    silkRuntime.setRow(silkPosition.getRow());
                    silkRuntime.setCol(silkPosition.getCol());
                    return silkRuntime;
                }).map(builder::add).ignoreElement();
                completables.add(completable);
            }
            final Single<List<SilkRuntime>> result$ = Single.fromCallable(() -> builder.build());
            return Completable.merge(completables).andThen(result$);
        }).flatMap(it -> it);
    }

    protected Single<List<SilkBarcode>> toSilkBarcodes(List<CheckSilkDTO> checkSilks) {
        final SilkBarcodeService silkBarcodeService = Jvertx.getProxy(SilkBarcodeService.class);
        return Flowable.fromIterable(checkSilks)
                .map(CheckSilkDTO::getCode)
                .flatMapSingle(silkBarcodeService::findBySilkCode).toList()
                .map(silkBarcodes -> {
                    final Map<String, SilkBarcode> map = silkBarcodes.stream().collect(Collectors.toMap(SilkBarcode::getCode, Function.identity()));
                    return checkSilks.stream()
                            .map(CheckSilkDTO::getCode)
                            .map(SilkBarcodeService::silkCodeToSilkBarCode)
                            .map(map::get)
                            .collect(Collectors.toList());
                });
    }

    protected List<SilkRuntime> checkPosition(List<SilkRuntime> silkRuntimes, List<CheckSilkDTO> checkSilks) throws DoffingTagException {
        for (CheckSilkDTO checkSilk : checkSilks) {
            final List<SilkRuntime> finds = silkRuntimes.stream().filter(silkRuntime ->
                    Objects.equals(silkRuntime.getSideType(), checkSilk.getSideType())
                            && Objects.equals(silkRuntime.getRow(), checkSilk.getRow())
                            && Objects.equals(silkRuntime.getCol(), checkSilk.getCol())
            ).collect(Collectors.toList());
            if (finds.size() == 1) {
                final SilkRuntime find = finds.get(0);
                if (Objects.equals(find.getSilk().getCode(), checkSilk.getCode())) {
                    continue;
                }
            }
            throw new DoffingTagException();
        }
        return silkRuntimes;
    }

    public List<SilkBarcode> checkBatchChange(List<SilkBarcode> silkBarcodes) throws BatchChangedException {
        final boolean present = silkBarcodes.stream().filter(it -> {
            final Batch printBatch = it.getBatch();
            final Batch batch = it.getLineMachine().getProductPlan().getBatch();
            return !Objects.equals(printBatch, batch);
        }).findFirst().isPresent();
        if (present) {
            throw new BatchChangedException();
        }
        return silkBarcodes;
    }
}
