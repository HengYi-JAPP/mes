package com.hengyi.japp.mes.auto.domain.data;

/**
 * 染判类型
 *
 * @author jzb 2018-07-25
 */
public enum DyeingType {
    /**
     * 一次染判，正常抽样
     */
    FIRST,
    /**
     * 交织染判，位与锭
     */
    CROSS_LINEMACHINE_SPINDLE,
    /**
     * 交织染判，位与位
     */
    CROSS_LINEMACHINE_LINEMACHINE,
    /**
     * 二次染判
     */
    SECOND,
    /**
     * 三次染判
     */
    THIRD,
}
