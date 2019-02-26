package com.hengyi.japp.mes.auto.exception;

import com.github.ixtf.japp.core.exception.JException;
import com.hengyi.japp.mes.auto.Constant;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import lombok.Getter;

/**
 * @author jzb 2018-07-28
 */
public class SilkCarNonEmptyException extends JException {
    @Getter
    private final SilkCar silkCar;

    public SilkCarNonEmptyException(SilkCar silkCar) {
        super(Constant.ErrorCode.SILKCAR_NON_EMPTY);
        this.silkCar = silkCar;
    }

    @Override
    public String getMessage() {
        return "丝车[" + silkCar.getCode() + "]非空";
    }
}
