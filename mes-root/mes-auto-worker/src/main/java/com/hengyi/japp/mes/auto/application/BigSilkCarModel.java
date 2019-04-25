package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.SilkCarType;
import com.hengyi.japp.mes.auto.dto.EntityByCodeDTO;
import com.hengyi.japp.mes.auto.exception.BatchChangedException;
import com.hengyi.japp.mes.auto.exception.DoffingCapacityException;
import com.hengyi.japp.mes.auto.exception.MultiBatchException;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeRepository;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * @author jzb 2018-11-15
 */
@Data
public class BigSilkCarModel {
    @EqualsAndHashCode.Include
    protected final SilkCar silkCar;
    protected final int silkCarCapacity;
    private final float count;

    protected BigSilkCarModel(SilkCar silkCar, float count) {
        Validate.isTrue(SilkCarType.BIG_SILK_CAR == silkCar.getType());
        this.silkCar = silkCar;
        this.silkCarCapacity = silkCar.getRow() * silkCar.getCol() * 2 * 2;
        this.count = count;
    }

    public static Collection<SilkRuntime> checkCapacity(List<SilkRuntime> silkRuntimes, int silkCarCapacity) throws Exception {
        final int silkCount = silkRuntimes.size();
        if (silkCount > silkCarCapacity) {
            throw new DoffingCapacityException();
        }
        return silkRuntimes;
    }

    public static Batch checkAndGetBatch(Collection<SilkBarcode> silkBarcodes) throws Exception {
        final Set<Batch> batches = silkBarcodes.parallelStream()
                .map(SilkBarcode::getBatch)
                .collect(toSet());
        if (batches.size() != 1) {
            throw new MultiBatchException();
        }
        return IterableUtils.get(batches, 0);
    }

    public Single<Collection<SilkRuntime>> generateSilkRuntimes(Collection<EntityByCodeDTO> checkSilks) {
        final SilkBarcodeRepository silkBarcodeRepository = Jvertx.getProxy(SilkBarcodeRepository.class);
        final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);

        return Flowable.fromIterable(checkSilks)
                .map(EntityByCodeDTO::getCode)
                .map(SilkBarcodeService::silkCodeToSilkBarCode)
                .flatMapSingle(silkBarcodeRepository::findByCode).toList()
                .map(Sets::newHashSet)
                .flatMap(silkBarcodes -> {
                    final int lineMachineCount = checkSilks.size();
                    final int checkSize = silkBarcodes.size();
                    Validate.isTrue(lineMachineCount == checkSize, "机台数错误");
                    final Batch silkBarcodeBatch = checkAndGetBatch(silkBarcodes);
                    return Flowable.fromIterable(silkBarcodes).flatMap(silkBarcode -> {
                        final LineMachine lineMachine = silkBarcode.getLineMachine();
                        final LineMachineProductPlan productPlan = lineMachine.getProductPlan();
                        final Batch batch = productPlan.getBatch();
                        if (!Objects.equals(silkBarcodeBatch, batch)) {
                            throw new BatchChangedException();
                        }
                        return Flowable.range(1, lineMachine.getSpindleNum()).flatMapSingle(spindle -> {
                            final String silkCode = silkBarcode.generateSilkCode(spindle);
                            return silkRepository.create().map(silk -> {
                                final SilkRuntime silkRuntime = new SilkRuntime();
                                silkRuntime.setSilk(silk);
                                silk.setCode(silkCode);
                                silk.setLineMachine(lineMachine);
                                silk.setSpindle(spindle);
                                silk.setDoffingNum(silkBarcode.getDoffingNum());
                                silk.setBatch(batch);
                                return silkRuntime;
                            });
                        });
                    }).toList().map(it -> checkCapacity(it, silkCarCapacity));
                });
    }
}
