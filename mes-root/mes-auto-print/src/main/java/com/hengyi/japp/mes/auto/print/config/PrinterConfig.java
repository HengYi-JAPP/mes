package com.hengyi.japp.mes.auto.print.config;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jzb 2018-08-18
 */
@Data
public class PrinterConfig {
    @NotBlank
    private String id;
    @NotBlank
    private String name;
}
