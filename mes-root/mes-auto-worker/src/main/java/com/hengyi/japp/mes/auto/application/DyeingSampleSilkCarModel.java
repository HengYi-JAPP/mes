package com.hengyi.japp.mes.auto.application;

import com.google.common.collect.ImmutableList;
import com.hengyi.japp.mes.auto.domain.Corporation;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author jzb 2018-11-15
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DyeingSampleSilkCarModel extends ManualSilkCarModel {
    @EqualsAndHashCode.Include
    protected final Workshop workshop;

    public DyeingSampleSilkCarModel(SilkCar silkCar, Workshop workshop) {
        super(silkCar, 1);
        this.workshop = workshop;
    }

    @Override
    public Single<List<CheckSilkDTO>> checkSilks() {
        final Corporation corporation = workshop.getCorporation();
//        if (corporation.getCode().equals("3000") && workshop.getCode().equals("B")) {
            final ImmutableList.Builder<CheckSilkDTO> builder = ImmutableList.builder();
            builder.add(SilkCarModel.shuffle(silkCar.checkSilks(SilkCarSideType.A)));
            return Single.just(builder.build());
//        }
//        throw new RuntimeException();
    }
}
