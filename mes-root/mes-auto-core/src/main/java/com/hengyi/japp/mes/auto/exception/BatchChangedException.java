package com.hengyi.japp.mes.auto.exception;

import com.github.ixtf.japp.core.exception.JException;
import com.hengyi.japp.mes.auto.Constant;

/**
 * @author jzb 2018-07-28
 */
public class BatchChangedException extends JException {
    public BatchChangedException() {
        super(Constant.ErrorCode.BATCH_CHANGED);
    }

    @Override
    public String getMessage() {
        return "机台换批，请重新打印标签";
    }
}
