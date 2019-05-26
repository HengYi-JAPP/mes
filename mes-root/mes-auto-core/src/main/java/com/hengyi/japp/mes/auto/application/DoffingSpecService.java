package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.domain.Line;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import io.reactivex.Flowable;

import java.util.List;

/**
 * @author jzb 2019-03-15
 */
public interface DoffingSpecService {

    List<CheckSilkDTO> checkSilks(DoffingType doffingType, Line line, SilkCar silkCar);

    Flowable<SilkRuntime> generateSilkRuntimes(DoffingType doffingType, Line line, SilkCar silkCar, List<CheckSilkDTO> checkSilks);

    List<CheckSilkDTO> checkSilks(DoffingType doffingType, Line line, SilkCar silkCar, int lineMachineCount);
}
