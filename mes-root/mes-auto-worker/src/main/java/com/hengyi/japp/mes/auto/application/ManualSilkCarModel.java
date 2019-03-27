package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.domain.*;
import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.hengyi.japp.mes.auto.application.SilkCarModel.shuffle;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2018-11-15
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ManualSilkCarModel extends AbstractSilkCarModel {
    @EqualsAndHashCode.Include
    protected final int lineMachineCount;
    @EqualsAndHashCode.Include
    private final float count;

    public ManualSilkCarModel(SilkCar silkCar, float count) {
        super(silkCar);
        this.count = count;
        this.lineMachineCount = (int) Math.ceil(count);
    }

    @Override
    public List<SilkCarPosition> getOrderedSilkPositions() {
        final ImmutableList.Builder<SilkCarPosition> builder = ImmutableList.builder();
        final int silkCarRow = silkCar.getRow();
        final int silkCarCol = silkCar.getCol();
        for (SilkCarSideType sideType : SilkCarSideType.values()) {
            int fillRowIndex = 1;
            for (int silkRow = silkCarRow; silkRow > 0; silkRow--) {
                if (fillRowIndex % 2 != 0) {
                    //奇数从左到右
                    for (int silkCol = 1; silkCol <= silkCarCol; silkCol++) {
                        final SilkCarPosition silkPosition = new SilkCarPosition();
                        silkPosition.setSideType(sideType);
                        silkPosition.setRow(silkRow);
                        silkPosition.setCol(silkCol);
                        builder.add(silkPosition);
                    }
                } else {
                    //偶数从右到左
                    for (int silkCol = silkCarCol; silkCol > 0; silkCol--) {
                        final SilkCarPosition silkPosition = new SilkCarPosition();
                        silkPosition.setSideType(sideType);
                        silkPosition.setRow(silkRow);
                        silkPosition.setCol(silkCol);
                        builder.add(silkPosition);
                    }
                }
                fillRowIndex++;
            }
        }
        return builder.build();
    }

    @Override
    public Single<List<SilkRuntime>> generateSilkRuntimes(List<CheckSilkDTO> checkSilks) {
        return toSilkBarcodes(checkSilks)
                .map(this::checkBatchChange)
                .flatMap(it -> generateSilkRuntimesBySilkBarcodes(ImmutableList.builder(), it))
                .map(it -> checkPosition(it, checkSilks));
    }

    protected Single<List<SilkRuntime>> generateSilkRuntimesBySilkBarcodes(ImmutableList.Builder<Single<SilkRuntime>> builder, List<SilkBarcode> silkBarcodes) {
        final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);

        for (SilkBarcode silkBarcode : silkBarcodes) {
            final LineMachine lineMachine = silkBarcode.getLineMachine();
            final Line line = lineMachine.getLine();
            final Workshop workshop = line.getWorkshop();
            if ("C".equals(workshop.getCode())) {
                return generateSilkRuntimesBySilkBarcodesC(builder, silkBarcodes);
            }
            for (int spindle : lineMachine.getSpindleSeq()) {
                final SilkRuntime silkRuntime = new SilkRuntime();
                final Single<SilkRuntime> silkRuntime$ = silkRepository.create().map(silk -> {
                    silkRuntime.setSilk(silk);
                    silk.setCode(silkBarcode.generateSilkCode(spindle));
                    silk.setDoffingNum(silkBarcode.getDoffingNum());
                    silk.setSpindle(spindle);
                    silk.setLineMachine(lineMachine);
                    silk.setBatch(silkBarcode.getBatch());
                    return silkRuntime;
                });
                builder.add(silkRuntime$);
            }
        }
        return addAll(builder.build());
    }

    private Single<List<SilkRuntime>> generateSilkRuntimesBySilkBarcodesC(ImmutableList.Builder<Single<SilkRuntime>> builder, List<SilkBarcode> silkBarcodes) {
        final int row = silkCar.getRow();
        final int col = silkCar.getCol();
        if (row != 3 || col != 4) {
            throw new RuntimeException("丝车不符!");
        }

        final Collection<Single<SilkRuntime>> build = builder.build();
        if (J.isEmpty(build)) {
            final int size = silkBarcodes.size();
            if (size == 1) {
                final List<Single<SilkRuntime>> collect = generateSilkRuntimesBySilkBarcodesC(SilkCarSideType.A, silkBarcodes.get(0)).collect(toList());
                return Single.merge(collect).toList();
            }
            if (size == 2) {
                final List<Single<SilkRuntime>> collect = Stream.concat(
                        generateSilkRuntimesBySilkBarcodesC(SilkCarSideType.A, silkBarcodes.get(0)),
                        generateSilkRuntimesBySilkBarcodesC(SilkCarSideType.B, silkBarcodes.get(1))
                ).collect(toList());
                return Single.merge(collect).toList();
            }
        } else {
            final int size = silkBarcodes.size();
            if (build.size() == 10 && size == 1) {
                final List<Single<SilkRuntime>> collect = generateSilkRuntimesBySilkBarcodesC(SilkCarSideType.B, silkBarcodes.get(1)).collect(toList());
                collect.addAll(build);
                return Single.merge(collect).toList();
            }
        }
        throw new RuntimeException("验证数不符!");
    }

    private Stream<Single<SilkRuntime>> generateSilkRuntimesBySilkBarcodesC(SilkCarSideType sideType, SilkBarcode silkBarcode) {
        final LineMachine lineMachine = silkBarcode.getLineMachine();
        final int spindleNum = lineMachine.getSpindleNum();
        if (spindleNum != 10) {
            throw new RuntimeException("丝锭数不符!");
        }
        return Stream.of(
                silkCarRuntime(sideType, 1, 3, silkBarcode, 1),
                silkCarRuntime(sideType, 1, 2, silkBarcode, 2),
                silkCarRuntime(sideType, 2, 1, silkBarcode, 3),
                silkCarRuntime(sideType, 2, 2, silkBarcode, 4),
                silkCarRuntime(sideType, 2, 3, silkBarcode, 5),
                silkCarRuntime(sideType, 2, 4, silkBarcode, 6),
                silkCarRuntime(sideType, 3, 4, silkBarcode, 7),
                silkCarRuntime(sideType, 3, 3, silkBarcode, 8),
                silkCarRuntime(sideType, 3, 2, silkBarcode, 9),
                silkCarRuntime(sideType, 3, 1, silkBarcode, 10)
        );
    }

    private Single<SilkRuntime> silkCarRuntime(SilkCarSideType sideType, int row, int col, SilkBarcode silkBarcode, int spindle) {
        final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);
        final SilkRuntime silkRuntime = new SilkRuntime();
        silkRuntime.setSideType(sideType);
        silkRuntime.setRow(row);
        silkRuntime.setCol(col);
        return silkRepository.create().map(silk -> {
            silkRuntime.setSilk(silk);
            silk.setCode(silkBarcode.generateSilkCode(spindle));
            silk.setDoffingNum(silkBarcode.getDoffingNum());
            silk.setSpindle(spindle);
            silk.setLineMachine(silkBarcode.getLineMachine());
            silk.setBatch(silkBarcode.getBatch());
            return silkRuntime;
        });
    }

    @Override
    public Single<List<CheckSilkDTO>> checkSilks() {
        final ImmutableList.Builder<CheckSilkDTO> builder = ImmutableList.builder();
        if (lineMachineCount == 1) {
            List<CheckSilkDTO> list = Lists.newArrayList();
            list.addAll(silkCar.checkSilks(SilkCarSideType.A));
            list.addAll(silkCar.checkSilks(SilkCarSideType.B));
            builder.add(shuffle(list));
            return Single.just(builder.build());
        }
        if (lineMachineCount == 2) {
            builder.add(shuffle(silkCar.checkSilks(SilkCarSideType.A)));
            builder.add(shuffle(silkCar.checkSilks(SilkCarSideType.B)));
            return Single.just(builder.build());
        }
        if (lineMachineCount == 3) {
            List<CheckSilkDTO> list = Lists.newArrayList();
            list.addAll(silkCar.checkSilks(SilkCarSideType.A, 3));
            list.addAll(silkCar.checkSilks(SilkCarSideType.A, 2));
            builder.add(shuffle(list));

            list = Lists.newArrayList();
            list.addAll(silkCar.checkSilks(SilkCarSideType.A, 1));
            list.addAll(silkCar.checkSilks(SilkCarSideType.B, 3));
            builder.add(shuffle(list));

            list = Lists.newArrayList();
            list.addAll(silkCar.checkSilks(SilkCarSideType.B, 2));
            list.addAll(silkCar.checkSilks(SilkCarSideType.B, 1));
            builder.add(shuffle(list));
            return Single.just(builder.build());
        }
        if (lineMachineCount == 4) {
            List<CheckSilkDTO> list = Lists.newArrayList();
            list.addAll(silkCar.checkSilks(SilkCarSideType.A, 4));
            list.addAll(silkCar.checkSilks(SilkCarSideType.A, 3));
            builder.add(shuffle(list));

            list = Lists.newArrayList();
            list.addAll(silkCar.checkSilks(SilkCarSideType.A, 2));
            list.addAll(silkCar.checkSilks(SilkCarSideType.A, 1));
            builder.add(shuffle(list));

            list = Lists.newArrayList();
            list.addAll(silkCar.checkSilks(SilkCarSideType.B, 4));
            list.addAll(silkCar.checkSilks(SilkCarSideType.B, 3));
            builder.add(shuffle(list));

            list = Lists.newArrayList();
            list.addAll(silkCar.checkSilks(SilkCarSideType.B, 2));
            list.addAll(silkCar.checkSilks(SilkCarSideType.B, 1));
            builder.add(shuffle(list));
            return Single.just(builder.build());
        }
        throw new RuntimeException();
    }

}
