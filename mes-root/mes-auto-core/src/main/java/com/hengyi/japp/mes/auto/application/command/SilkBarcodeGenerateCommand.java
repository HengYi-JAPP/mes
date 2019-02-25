package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;


/**
 * @author jzb 2018-06-21
 */
@Data
public class SilkBarcodeGenerateCommand implements Serializable {
    @NotNull
    private EntityDTO lineMachine;
    @NotNull
//    private LocalDate codeDate;
    private Date codeDate;
    @NotBlank
    private String doffingNum;

    public String getDoffingNum() {
        return doffingNum.toUpperCase().trim();
    }

    @Data
    public static class Batch implements Serializable {
        @NotNull
        @Size(min = 1)
        private Collection<SilkBarcodeGenerateCommand> commands;
    }
}
