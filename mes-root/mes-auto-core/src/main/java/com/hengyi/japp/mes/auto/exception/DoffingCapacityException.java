package com.hengyi.japp.mes.auto.exception;

import com.github.ixtf.japp.core.exception.JException;
import com.hengyi.japp.mes.auto.Constant;

/**
 * @author jzb 2018-07-28
 */
public class DoffingCapacityException extends JException {
    public DoffingCapacityException() {
        super(Constant.ErrorCode.DOFFING_CAPACITY);
    }

    @Override
    public String getMessage() {
        return "丝车容量超出";
    }
}
