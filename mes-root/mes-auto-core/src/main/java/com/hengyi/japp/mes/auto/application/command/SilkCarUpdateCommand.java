package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.domain.data.SilkCarType;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;


/**
 * @author jzb 2018-06-21
 */
@Data
public class SilkCarUpdateCommand implements Serializable {
    @NotNull
    private SilkCarType type;
    @NotBlank
    private String number;
    @NotBlank
    private String code;
    @Min(1)
    private int row;
    @Min(1)
    private int col;

    @Data
    public static class Batch implements Serializable {
        @NotNull
        @Size(min = 1)
        private Collection<SilkCarUpdateCommand> commands;
    }
}
