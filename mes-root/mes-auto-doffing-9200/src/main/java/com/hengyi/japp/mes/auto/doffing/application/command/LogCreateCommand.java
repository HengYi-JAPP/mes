package com.hengyi.japp.mes.auto.doffing.application.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author jzb 2019-04-03
 */
@Data
public class LogCreateCommand implements Serializable {
    // info error
    @NotBlank
    private String level;
    private String message;
    private String command;
    @NotBlank
    private String operatorName;
}
