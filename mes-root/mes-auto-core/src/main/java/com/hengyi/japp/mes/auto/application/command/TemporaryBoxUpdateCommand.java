package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


/**
 * @author jzb 2018-06-21
 */
@Data
public class TemporaryBoxUpdateCommand implements Serializable {
    @NotBlank
    private String code;
    @NotNull
    private EntityDTO batch;
    @NotNull
    private EntityDTO grade;

}
