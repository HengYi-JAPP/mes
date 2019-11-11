package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.ImmutableList;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.exception.DoffingTagException;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.hengyi.japp.mes.auto.application.AutoSilkCarModelConfigRegistry.*;

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
        config = Jvertx.getProxy(AutoSilkCarModelConfigRegistry.class).find(silkCar, workshop);
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
                if (workshop.getCode().startsWith("HF")) {
                    final int row = orderBy;
                    final Single<SilkRuntime> silkRuntime$ = silkRepository.create().map(silk -> {
                        silkRuntime.setSilk(silk);
                        silk.setCode(silkBarcode.generateSilkCode(spindle));
                        silk.setDoffingNum(silkBarcode.getDoffingNum());
                        silk.setSpindle(spindle);
                        silk.setLineMachine(lineMachine);
                        silk.setBatch(lineMachine.getProductPlan().getBatch());

                        switch (spindle) {
                            case 1: {
                                silkRuntime.setSideType(SilkCarSideType.A);
                                silkRuntime.setRow(row);
                                silkRuntime.setCol(1);
                                return silkRuntime;
                            }
                            case 2: {
                                silkRuntime.setSideType(SilkCarSideType.A);
                                silkRuntime.setRow(row);
                                silkRuntime.setCol(3);
                                return silkRuntime;
                            }
                            case 3: {
                                silkRuntime.setSideType(SilkCarSideType.A);
                                silkRuntime.setRow(row);
                                silkRuntime.setCol(5);
                                return silkRuntime;
                            }
                            case 4: {
                                silkRuntime.setSideType(SilkCarSideType.B);
                                silkRuntime.setRow(row);
                                silkRuntime.setCol(2);
                                return silkRuntime;
                            }
                            case 5: {
                                silkRuntime.setSideType(SilkCarSideType.B);
                                silkRuntime.setRow(row);
                                silkRuntime.setCol(4);
                                return silkRuntime;
                            }
                            case 6: {
                                silkRuntime.setSideType(SilkCarSideType.B);
                                silkRuntime.setRow(row);
                                silkRuntime.setCol(6);
                                return silkRuntime;
                            }
                        }
                        throw new DoffingTagException();
                    });
                    builder.add(silkRuntime$);
                    continue;
                }


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
        // 合股丝
        if ("F".equals(workshop.getCode())) {
            Single.fromCallable(() -> {
                final Stream<CheckSilkDTO> streamA = Stream.of(3, 5).map(col -> {
                    final CheckSilkDTO dto = new CheckSilkDTO();
                    dto.setSideType(SilkCarSideType.A);
                    dto.setCol(col);
                    return dto;
                });
                final Stream<CheckSilkDTO> streamB = Stream.of(2, 4, 6).map(col -> {
                    final CheckSilkDTO dto = new CheckSilkDTO();
                    dto.setSideType(SilkCarSideType.B);
                    dto.setCol(col);
                    return dto;
                });
                final List<CheckSilkDTO> collectList = Stream.concat(streamA, streamB).collect(Collectors.toList());
                return IntStream.rangeClosed(1, 3).mapToObj(row -> {
                    Collections.shuffle(collectList);
                    final CheckSilkDTO copyDTO = collectList.get(0);
                    final CheckSilkDTO dto = new CheckSilkDTO();
                    dto.setRow(row);
                    dto.setSideType(copyDTO.getSideType());
                    dto.setCol(copyDTO.getCol());
                    return dto;
                }).collect(Collectors.toList());
            });
        }
        return Single.fromCallable(() -> config.checkSilks());
    }

    protected Single<List<SilkRuntime>> adminGenerateSilkRuntimesBySilkBarcodes(List<SilkBarcode> silkBarcodes) {
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
                    silk.setBatch(silk.getBatch());

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

}
