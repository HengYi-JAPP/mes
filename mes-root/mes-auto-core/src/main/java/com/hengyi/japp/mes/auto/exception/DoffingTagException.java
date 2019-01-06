package com.hengyi.japp.mes.auto.exception;

import com.github.ixtf.japp.core.exception.JException;
import com.hengyi.japp.mes.auto.Constant;

/**
 * @author jzb 2018-07-28
 */
public class DoffingTagException extends JException {
    public DoffingTagException() {
        super(Constant.ErrorCode.DOFFING_TAG);
    }

    @Override
    public String getMessage() {
        return "标签检查有误";
    }
}
