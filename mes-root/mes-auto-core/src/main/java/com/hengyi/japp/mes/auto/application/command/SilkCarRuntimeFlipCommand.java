package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.dto.SilkCarRecordDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


/**
 * @author jzb 2018-06-21
 */
@Data
public class SilkCarRuntimeFlipCommand implements Serializable {
    @NotNull
    private SilkCarRecordDTO silkCarRecord;
}
