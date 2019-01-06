package com.hengyi.japp.mes.auto.interfaces.facevisa.dto;

import com.github.ixtf.japp.core.J;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * @author jzb 2018-10-20
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AutoVisualInspectionSilkInfoDTO implements Serializable {
    private String silk_code;
    private String workshop_id;
    private String workshop_name;
    private String line_id;
    private String line_name;
    /**
     * 机台号
     */
    private int item;
    private int spindle_no;
    /**
     * 落次
     */
    private String fall_no;
    private String batch_no;
    private String spec;
    /**
     * 落丝时间
     */
    private Date product_date_time;
    private Collection<ExceptionInfo> exceptionInfos;

    public boolean isIs_exception() {
        return J.nonEmpty(exceptionInfos);
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class ExceptionInfo implements Serializable {
        private String exception_id;
        private String exception_name;
        private String operator_id;
        private String operator_display_name;
    }
}
