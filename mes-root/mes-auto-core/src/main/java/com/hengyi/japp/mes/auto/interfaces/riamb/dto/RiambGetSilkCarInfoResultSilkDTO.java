package com.hengyi.japp.mes.auto.interfaces.riamb.dto;

import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import lombok.Data;

import java.io.Serializable;

/**
 * @author jzb 2018-06-22
 */
@Data
public class RiambGetSilkCarInfoResultSilkDTO implements Serializable {
    private final SilkCarRuntime silkCar;

    public RiambGetSilkCarInfoResultSilkDTO(SilkCarRuntime silkCar) {
        this.silkCar = silkCar;
    }
}
