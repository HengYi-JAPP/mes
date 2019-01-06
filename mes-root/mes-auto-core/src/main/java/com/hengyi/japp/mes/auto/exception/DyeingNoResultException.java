package com.hengyi.japp.mes.auto.exception;

import com.github.ixtf.japp.core.exception.JException;
import com.hengyi.japp.mes.auto.Constant;

/**
 * @author jzb 2018-07-28
 */
public class DyeingNoResultException extends JException {
    public DyeingNoResultException() {
        super(Constant.ErrorCode.DYEING_NO_RESULT);
    }

    @Override
    public String getMessage() {
        return "染判结果未出";
    }
}
