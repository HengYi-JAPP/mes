package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.domain.LineMachine;
import com.hengyi.japp.mes.auto.domain.SilkBarcode;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static com.hengyi.japp.mes.auto.application.SilkCarModel.shuffle;

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
            for (int spindle : lineMachine.getSpindleSeq()) {
                final SilkRuntime silkRuntime = new SilkRuntime();
                final Single<SilkRuntime> silkRuntime$ = silkRepository.create().map(silk -> {
                    silkRuntime.setSilk(silk);
                    silk.setCode(silkBarcode.generateSilkCode(spindle));
                    silk.setDoffingNum(silkBarcode.getDoffingNum());
                    silk.setSpindle(spindle);
                    silk.setLineMachine(lineMachine);
//                    silk.setBatch(lineMachine.getProductPlan().getBatch());
                    silk.setBatch(silkBarcode.getBatch());
                    return silkRuntime;
                });
                builder.add(silkRuntime$);
            }
        }
        return addAll(builder.build());
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
