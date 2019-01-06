package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.Workshop;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author jzb 2018-11-15
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PhysicalInfoSilkCarModel extends DyeingSampleSilkCarModel {

    public PhysicalInfoSilkCarModel(SilkCar silkCar, Workshop workshop) {
        super(silkCar, workshop);
    }

}
