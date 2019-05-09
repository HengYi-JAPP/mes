package com.hengyi.japp.mes.auto.application;

import com.hengyi.japp.mes.auto.domain.LineMachineProductPlan;
import com.hengyi.japp.mes.auto.domain.Operator;
import com.hengyi.japp.mes.auto.domain.Silk;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import com.hengyi.japp.mes.auto.interfaces.jikon.dto.GetSilkSpindleInfoDTO;

import java.util.List;

/**
 * @author jzb 2018-06-25
 */
public interface ApplicationEvents {

    void fire(LineMachineProductPlan lineMachineProductPlan);

    void fire(SilkCarRuntime silkCarRuntime, GetSilkSpindleInfoDTO dto, List<String> reasons);

    void fire(Silk silk, Operator operator);

    void fire(SilkCarRuntime silkCarRuntime);

}
