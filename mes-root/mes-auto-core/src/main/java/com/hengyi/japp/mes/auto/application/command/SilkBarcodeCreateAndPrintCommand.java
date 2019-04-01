package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.domain.data.MesAutoPrinter;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;


/**
 * @author liuyuan 2018-03-31
 */
@Data
public class SilkBarcodeCreateAndPrintCommand extends SilkBarcodeGenerateCommand implements Serializable {

    @Data
    public static class Batch implements Serializable {
        @NotNull
        @Size(min = 1)
        private Collection<SilkBarcodeCreateAndPrintCommand> commands;
        @NotNull
        private MesAutoPrinter mesAutoPrinter;
    }
}
