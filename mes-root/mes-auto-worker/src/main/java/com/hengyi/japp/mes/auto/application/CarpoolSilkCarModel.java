package com.hengyi.japp.mes.auto.application;

import com.github.ixtf.japp.vertx.Jvertx;
import com.google.common.collect.ImmutableList;
import com.hengyi.japp.mes.auto.config.MesAutoConfig;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.repository.SilkRepository;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author jzb 2018-11-15
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CarpoolSilkCarModel extends AbstractSilkCarModel {

    public CarpoolSilkCarModel(SilkCar silkCar) {
        super(silkCar);
    }

    @Override
    public Single<List<SilkRuntime>> generateSilkRuntimes(List<CheckSilkDTO> checkSilks) {
        final SilkRepository silkRepository = Jvertx.getProxy(SilkRepository.class);

        final ImmutableList.Builder<Single<SilkRuntime>> builder = ImmutableList.builder();
        final List<SilkCarPosition> orderedSilkPositions = getOrderedSilkPositions();
        int posIndex = 0;
        for (CheckSilkDTO checkSilk : checkSilks) {
            final SilkCarPosition silkPosition = orderedSilkPositions.get(posIndex++);
            final SilkRuntime silkRuntime = new SilkRuntime();
            silkRuntime.setSideType(silkPosition.getSideType());
            silkRuntime.setRow(silkPosition.getRow());
            silkRuntime.setCol(silkPosition.getCol());
            final Single<SilkRuntime> silkRuntime$ = silkRepository.findByCode(checkSilk.getCode()).toSingle().map(silk -> {
                if (!silk.isDetached()) {
                    final String msg = "checkSilk[" + checkSilk.getCode() + "]，非解绑丝锭拼车";
                    log.error(msg);
                    //todo 解绑逻辑更新
//                    throw new RuntimeException(msg);
                }
                if (silk.isDyeingSample()) {
                    final String msg = "checkSilk[" + checkSilk.getCode() + "]，标样丝拼车";
                    throw new RuntimeException(msg);
                }
                if (silk.getTemporaryBox() != null) {
                    final String msg = "checkSilk[" + checkSilk.getCode() + "]，暂存箱丝锭拼车";
                    throw new RuntimeException(msg);
                }
                silk.setDetached(false);
                silkRuntime.setSilk(silk);
                return silkRuntime;
            });
            builder.add(silkRuntime$);
        }
        return addAll(builder.build());
    }

    @Override
    public List<SilkCarPosition> getOrderedSilkPositions() {
        final MesAutoConfig mesAutoConfig = Jvertx.getProxy(MesAutoConfig.class);
        return mesAutoConfig.getCarpoolSilkCarModelOrderType().getOrderedSilkPositions(silkCar);
    }

    @Override
    public Single<List<CheckSilkDTO>> checkSilks() {
        throw new IllegalAccessError();
    }

}
