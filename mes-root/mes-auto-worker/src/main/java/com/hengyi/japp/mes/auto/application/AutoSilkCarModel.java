package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.ImmutableList;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import com.hengyi.japp.mes.auto.domain.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static com.hengyi.japp.mes.auto.application.AutolSilkCarModelConfigRegistry.*;

/**
 * todo 配置文件形式，自定义落筒顺序
 *
 * @author jzb 2018-11-15
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AutoSilkCarModel extends AbstractSilkCarModel {
    @EqualsAndHashCode.Include
    private final Workshop workshop;
    private final Config config;

    public AutoSilkCarModel(SilkCar silkCar, Workshop workshop) {
        super(silkCar);
        this.workshop = workshop;
        config = Jvertx.getProxy(AutolSilkCarModelConfigRegistry.class).find(silkCar, workshop);
    }

    @Override
    public List<SilkCarPosition> getOrderedSilkPositions() {
        return config.getOrderedSilkPositions();
    }

    @Override
    public Single<List<SilkRuntime>> generateSilkRuntimes(List<CheckSilkDTO> checkSilks) {
        return toSilkBarcodes(checkSilks)
                .map(this::checkBatchChange)
                .flatMap(this::generateSilkRuntimesBySilkBarcodes)
                .map(it -> checkPosition(it, checkSilks));
    }

    private Single<List<SilkRuntime>> generateSilkRuntimesBySilkBarcodes(List<SilkBarcode> silkBarcodes) {
        final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);
        final ImmutableList.Builder<Single<SilkRuntime>> builder = ImmutableList.builder();
        for (int orderBy = 0; orderBy < silkBarcodes.size(); orderBy++) {
            final SilkBarcode silkBarcode = silkBarcodes.get(orderBy);
            final LineMachine lineMachine = silkBarcode.getLineMachine();
            final LineMachineSpec lineMachineSpec = config.getLineMachineSpecs().get(orderBy);
            for (int spindle : lineMachine.getSpindleSeq()) {
                final SilkRuntime silkRuntime = new SilkRuntime();
                final Single<SilkRuntime> silkRuntime$ = silkRepository.create().map(silk -> {
                    silkRuntime.setSilk(silk);
                    silk.setCode(silkBarcode.generateSilkCode(spindle));
                    silk.setDoffingNum(silkBarcode.getDoffingNum());
                    silk.setSpindle(spindle);
                    silk.setLineMachine(lineMachine);
                    silk.setBatch(lineMachine.getProductPlan().getBatch());

                    final LineMachineSilkSpec lineMachineSilkSpec = lineMachineSpec.findSilkSpecBySpindle(spindle);
                    silkRuntime.setSideType(lineMachineSilkSpec.getSideType());
                    silkRuntime.setRow(lineMachineSilkSpec.getRow());
                    silkRuntime.setCol(lineMachineSilkSpec.getCol());
                    return silkRuntime;
                });
                builder.add(silkRuntime$);
            }
        }
        return Flowable.fromIterable(builder.build()).flatMapSingle(it -> it).toList();
    }

    @Override
    public Single<List<CheckSilkDTO>> checkSilks() {
        return Single.fromCallable(() -> config.checkSilks());
    }

}
