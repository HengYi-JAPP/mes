package com.hengyi.japp.mes.auto.application;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.domain.SilkCarRecord;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.hengyi.japp.mes.auto.application.SilkCarModel.shuffle;

/**
 * @author jzb 2018-11-15
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AppendSilkCarModel extends ManualSilkCarModel {
    private final SilkCarRuntime silkCarRuntime;

    public AppendSilkCarModel(SilkCarRuntime silkCarRuntime, float count) {
        super(silkCarRuntime.getSilkCarRecord().getSilkCar(), count);
        this.silkCarRuntime = silkCarRuntime;
    }

    @Override
    public Single<List<SilkRuntime>> generateSilkRuntimes(List<CheckSilkDTO> checkSilks) {
        return toSilkBarcodes(checkSilks)
                .map(this::checkBatchChange)
                .flatMap(silkBarcodes -> {
                    final ImmutableList.Builder<Single<SilkRuntime>> builder = ImmutableList.builder();
                    final SilkCarRecord silkCarRecord = silkCarRuntime.getSilkCarRecord();
//                    final Collection<SilkRuntime> initSilkRuntimes = silkCarRecord.initSilks();
//                    initSilkRuntimes.stream().map(Single::just).forEach(builder::add);
                    silkCarRuntime.getSilkRuntimes().stream().map(Single::just).forEach(builder::add);
                    return generateSilkRuntimesBySilkBarcodes(builder, silkBarcodes).map(it -> {
                        final List<SilkRuntime> result = Lists.newArrayList(it);
                        result.removeAll(silkCarRuntime.getSilkRuntimes());
                        return result;
                    });
                })
                .map(it -> checkPosition(it, checkSilks));
    }

    @Override
    public Single<List<CheckSilkDTO>> checkSilks() {
        final ImmutableList.Builder<CheckSilkDTO> builder = ImmutableList.builder();
        if (lineMachineCount == 1) {
            builder.add(shuffle(silkCar.checkSilks(SilkCarSideType.B)));
            return Single.just(builder.build());
        }
        throw new RuntimeException();
    }

}
