package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.Sets;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.data.SilkCarType;
import com.hengyi.japp.mes.auto.dto.EntityByCodeDTO;
import com.hengyi.japp.mes.auto.repository.SilkBarcodeRepository;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

/**
 * @author jzb 2018-11-15
 */
@Data
public class BigSilkCarModel {
    @EqualsAndHashCode.Include
    protected final SilkCar silkCar;
    protected final int silkCarCapacity;

    protected BigSilkCarModel(SilkCar silkCar) {
        Validate.isTrue(SilkCarType.BIG_SILK_CAR == silkCar.getType());
        this.silkCar = silkCar;
        this.silkCarCapacity = silkCar.getRow() * silkCar.getCol() * 2 * 2;
    }

    public Flowable<SilkRuntime> generateSilkRuntimes(Collection<EntityByCodeDTO> checkSilks) {
        final SilkBarcodeRepository silkBarcodeRepository = Jvertx.getProxy(SilkBarcodeRepository.class);
        final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);

        return Flowable.fromIterable(checkSilks)
                .map(EntityByCodeDTO::getCode)
                .flatMapSingle(silkBarcodeRepository::findByCode).toList()
                .map(Sets::newHashSet)
                .flatMapPublisher(silkBarcodes -> {
                    final int lineMachineCount = checkSilks.size();
                    final int checkSize = silkBarcodes.size();
                    Validate.isTrue(lineMachineCount == checkSize, "机台数错误");
                    return Flowable.fromIterable(silkBarcodes).flatMap(silkBarcode -> {
                        final LineMachine lineMachine = silkBarcode.getLineMachine();
                        final Set<String> silkCodes = IntStream.rangeClosed(1, lineMachine.getItem()).mapToObj(silkBarcode::generateSilkCode).collect(toSet());
                        return Flowable.fromIterable(silkCodes)
                                .flatMapMaybe(silkRepository::findByCode)
                                .map(silk -> {
                                    final SilkRuntime silkRuntime = new SilkRuntime();
                                    silkRuntime.setSilk(silk);
                                    return silkRuntime;
                                });
                    });
                });
    }
}
