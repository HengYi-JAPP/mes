package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author jzb 2019-02-21
 */
@Data
public class ReportCommand implements Serializable {
    @NotNull
    private EntityDTO workshop;
    @NotNull
    private Date startDate;
    @NotNull
    private Date endDate;
    private List<EntityDTO> packageClasses;
}
