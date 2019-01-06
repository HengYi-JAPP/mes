package com.hengyi.japp.mes.auto.print.application.command;

import com.github.ixtf.japp.vertx.Jvertx;
import com.hengyi.japp.mes.auto.print.application.config.SilkPrintConfig;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * @author jzb 2018-08-16
 */
@Data
public class SilkPrintCommand implements Serializable {
    @NotNull
    @Size(min = 1)
    private Collection<Item> silks;

    public SilkPrintable toPrintable() {
        final SilkPrintConfig silkPrintConfig = Jvertx.getProxy(SilkPrintConfig.class);
        return new SilkPrintable(silkPrintConfig, this);
    }

    @Data
    public static class Item implements Serializable {
        @NotBlank
        private String code;
        @NotNull
        private Date codeDate;
        @NotBlank
        private String lineName;
        @NotBlank
        private int lineMachineItem;
        @Min(1)
        private int spindle;
        @NotBlank
        private String doffingNum;
        @NotBlank
        private String batchNo;
        @NotBlank
        private String batchSpec;
    }

}
