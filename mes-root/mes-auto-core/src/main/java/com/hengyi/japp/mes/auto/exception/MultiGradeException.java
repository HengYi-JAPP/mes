package com.hengyi.japp.mes.auto.exception;

import com.github.ixtf.japp.core.exception.JException;
import com.hengyi.japp.mes.auto.Constant;

/**
 * @author jzb 2018-07-28
 */
public class MultiGradeException extends JException {
    public MultiGradeException() {
        super(Constant.ErrorCode.MULTI_GRADE);
    }

    @Override
    public String getMessage() {
        return "混等";
    }
}
