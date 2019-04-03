package com.hengyi.japp.mes.auto.doffing.application.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jzb 2019-04-03
 */
@Data
public class LogCreateCommand {
    // info error
    @NotBlank
    private String level;
    private String message;
    @NotBlank
    private String operatorName;
}
