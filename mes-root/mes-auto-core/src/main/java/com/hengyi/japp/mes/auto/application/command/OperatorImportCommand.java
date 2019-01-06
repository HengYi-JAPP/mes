package com.hengyi.japp.mes.auto.application.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;


/**
 * @author jzb 2018-06-22
 */
@Data
public class OperatorImportCommand implements Serializable {
    private String hrId;
    private String oaId;
    @NotBlank
    private String name;

}
