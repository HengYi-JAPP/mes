package com.hengyi.japp.mes.auto.exception;

import com.github.ixtf.japp.core.exception.JException;
import com.hengyi.japp.mes.auto.Constant;

/**
 * @author jzb 2018-07-28
 */
public class SilkCarNonEmptyException extends JException {
    public SilkCarNonEmptyException() {
        super(Constant.ErrorCode.SILKCAR_NON_EMPTY);
    }

    @Override
    public String getMessage() {
        return "丝车非空";
    }
}
