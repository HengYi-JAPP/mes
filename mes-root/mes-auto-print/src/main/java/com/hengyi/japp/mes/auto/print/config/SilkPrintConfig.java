package com.hengyi.japp.mes.auto.print.config;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author jzb 2018-08-18
 */
@Data
public class SilkPrintConfig implements Serializable {
    @NotBlank
    private String corpPrefix;
    @NotNull
    private PaperConfig paperConfig;
    @NotNull
    private FontConfig fontConfig;
    @NotNull
    private BarcodeConfig barcodeConfig;
    @NotNull
    private ZxingConfig zxingConfig;


}
