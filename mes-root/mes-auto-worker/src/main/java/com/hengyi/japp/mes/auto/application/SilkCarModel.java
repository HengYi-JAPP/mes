package com.hengyi.japp.mes.auto.application;

import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import com.hengyi.japp.mes.auto.domain.dto.CheckSilkDTO;
import io.reactivex.Single;

import java.util.Collections;
import java.util.List;

/**
 * 丝锭摆放模型
 *
 * @author jzb 2018-11-15
 */
public interface SilkCarModel {

    static CheckSilkDTO shuffle(Iterable<CheckSilkDTO> list) {
        final List<CheckSilkDTO> copy = Lists.newArrayList(list);
        Collections.shuffle(copy);
        return copy.get(0);
    }

    static Single<SilkCarModel> auto(Single<SilkCar> silkCar$, Single<Workshop> workshop$) {
        return silkCar$.flatMap(silkCar -> workshop$.map(workshop -> new AutoSilkCarModel(silkCar, workshop)));
    }

    static Single<SilkCarModel> manual(Single<SilkCar> silkCar$, float count) {
        return silkCar$.map(silkCar -> new ManualSilkCarModel(silkCar, count));
    }

    static Single<SilkCarModel> dyeingSample(Single<SilkCar> silkCar$, Single<Workshop> workshop$) {
        return silkCar$.flatMap(silkCar -> workshop$.map(workshop -> new DyeingSampleSilkCarModel(silkCar, workshop)));
    }

    static Single<SilkCarModel> physicalInfo(Single<SilkCar> silkCar$, Single<Workshop> workshop$) {
        return silkCar$.flatMap(silkCar -> workshop$.map(workshop -> new PhysicalInfoSilkCarModel(silkCar, workshop)));
    }

    static Single<SilkCarModel> carpool(Single<SilkCar> silkCar$) {
        return silkCar$.map(silkCar -> new CarpoolSilkCarModel(silkCar));
    }

    static Single<SilkCarModel> append(Single<SilkCarRuntime> silkCarRuntime$, float count) {
        return silkCarRuntime$.map(silkCarRuntime -> new AppendSilkCarModel(silkCarRuntime, count));
    }

    SilkCar getSilkCar();

    List<SilkCarPosition> getOrderedSilkPositions();

    Single<List<SilkRuntime>> generateSilkRuntimes(List<CheckSilkDTO> checkSilks);

    Single<List<CheckSilkDTO>> checkSilks();

}
