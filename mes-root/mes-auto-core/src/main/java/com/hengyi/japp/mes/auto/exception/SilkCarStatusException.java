package com.hengyi.japp.mes.auto.exception;

import com.github.ixtf.japp.core.exception.JException;
import com.hengyi.japp.mes.auto.Constant;
import com.hengyi.japp.mes.auto.domain.SilkCar;

/**
 * @author jzb 2018-07-28
 */
public class SilkCarStatusException extends JException {
    private final SilkCar silkCar;

    public SilkCarStatusException(SilkCar silkCar) {
        super(Constant.ErrorCode.SILK_CAR_STATUS);
        this.silkCar = silkCar;
    }

    @Override
    public String getMessage() {
        return "丝车[" + silkCar.getCode() + "]，状态异常";
    }
}
