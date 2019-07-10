package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


/**
 * @author jzb 2018-06-22
 */
@Data
public class ExceptionRecordUpdateCommand implements Serializable {
    @NotNull
    private EntityDTO lineMachine;
    @Min(1)
    private int spindle;
    @NotBlank
    private String doffingNum;
    @NotNull
    private EntityDTO exception;

}
