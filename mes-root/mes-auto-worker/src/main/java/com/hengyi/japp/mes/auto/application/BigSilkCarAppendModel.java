package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
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

/**
 * @author jzb 2018-11-15
 */
@Data
public class BigSilkCarAppendModel {
    @EqualsAndHashCode.Include
    protected final SilkCarRuntime silkCarRuntime;
    protected final int silkCarCapacity;
    protected final float count;

    protected BigSilkCarAppendModel(SilkCarRuntime silkCarRuntime, float count) {
        this.silkCarRuntime = silkCarRuntime;
        this.count = count;
        final SilkCar silkCar = silkCarRuntime.getSilkCarRecord().getSilkCar();
        Validate.isTrue(SilkCarType.BIG_SILK_CAR == silkCar.getType());
        this.silkCarCapacity = silkCar.getRow() * silkCar.getCol() * 2 * 2 - silkCarRuntime.getSilkRuntimes().size();
    }

    public Flowable<SilkRuntime> generateSilkRuntimes(Collection<EntityByCodeDTO> checkSilks) {
        final SilkBarcodeRepository silkBarcodeRepository = Jvertx.getProxy(SilkBarcodeRepository.class);
        final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);

//        return Flowable.fromIterable(checkSilks)
//                .map(EntityByCodeDTO::getCode)
//                .map(SilkBarcodeService::silkCodeToSilkBarCode)
//                .flatMapSingle(silkBarcodeRepository::findByCode).toList()
//                .map(Sets::newHashSet)
//                .flatMapPublisher(silkBarcodes -> {
//                    final int lineMachineCount = checkSilks.size();
//                    final int checkSize = silkBarcodes.size();
//                    Validate.isTrue(lineMachineCount == checkSize, "机台数错误");
//                    final Batch silkBarcodeBatch = checkAndGetBatch(silkBarcodes);
//                    return Flowable.fromIterable(silkBarcodes).flatMap(silkBarcode -> {
//                        final LineMachine lineMachine = silkBarcode.getLineMachine();
//                        final LineMachineProductPlan productPlan = lineMachine.getProductPlan();
//                        final Batch batch = productPlan.getBatch();
//                        if (!Objects.equals(silkBarcodeBatch, batch)) {
//                            throw new BatchChangedException();
//                        }
//                        return Flowable.range(1, lineMachine.getItem()).flatMapSingle(spindle -> {
//                            final String silkCode = silkBarcode.generateSilkCode(spindle);
//                            return silkRepository.create().map(silk -> {
//                                final SilkRuntime silkRuntime = new SilkRuntime();
//                                silkRuntime.setSilk(silk);
//                                silk.setCode(silkCode);
//                                silk.setLineMachine(lineMachine);
//                                silk.setSpindle(spindle);
//                                silk.setDoffingNum(silkBarcode.getDoffingNum());
//                                silk.setBatch(batch);
//                                return silkRuntime;
//                            });
//                        });
//                    });
//                });

        return null;
    }
}
