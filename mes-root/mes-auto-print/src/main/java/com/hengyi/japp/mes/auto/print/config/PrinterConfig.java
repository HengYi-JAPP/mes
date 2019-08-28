package com.hengyi.japp.mes.auto.print.config;

import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * @author jzb 2018-08-18
 */
@Data
public class PrinterConfig {
    @Getter
    private static final String id = uuid();
    @NotBlank
    private String name;

    private static String uuid() {
        final String uuid = UUID.randomUUID().toString();
        return uuid.replace("-", "");
    }
}
