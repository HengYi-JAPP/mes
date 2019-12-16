package com.hengyi.japp.mes.auto.report.application.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author jzb 2019-12-16
 */
@Data
public class ExceptionRecordByClassReportCommand implements Serializable {
    @NotBlank
    private String workshopId;
    @NotNull
    private Date startDateTime;
    @NotNull
    private Date endDateTime;
}
