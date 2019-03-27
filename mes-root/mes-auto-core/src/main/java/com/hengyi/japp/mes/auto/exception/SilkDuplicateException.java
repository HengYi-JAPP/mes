package com.hengyi.japp.mes.auto.exception;

import com.github.ixtf.japp.core.exception.JException;
import com.hengyi.japp.mes.auto.Constant;
import com.hengyi.japp.mes.auto.domain.Silk;

/**
 * @author jzb 2018-11-23
 */
public class SilkDuplicateException extends JException {
    private final Silk silk;

    public SilkDuplicateException(Silk silk) {
        super(Constant.ErrorCode.SILK_DUPLICATE);
        this.silk = silk;
    }

    @Override
    public String getMessage() {
        return "Silk[" + silk.getCode() + "],丝锭重复落筒";
    }
}
