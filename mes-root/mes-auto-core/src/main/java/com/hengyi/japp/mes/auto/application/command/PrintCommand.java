package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.domain.dto.EntityDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * @author jzb 2018-08-24
 */
@Data
public abstract class PrintCommand implements Serializable {
    @NotNull
    private EntityDTO mesAutoPrinter;

    /**
     * @author jzb 2018-08-24
     */
    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class SilkPrintCommand extends PrintCommand {
        @NotNull
        @Size(min = 1)
        private Collection<Item> silks;
    }

    /**
     * @author jzb 2018-08-24
     */
    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class SilkBarcodePrintCommand extends PrintCommand {
        @NotNull
        @Size(min = 1)
        private Collection<EntityDTO> silkBarcodes;
    }

    @Data
    public static class Item implements Serializable, Comparable<Item> {
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

        @Override
        public int compareTo(Item o) {
            int i = codeDate.compareTo(o.codeDate);
            if (i != 0) {
                return i;
            }
            i = lineName.compareTo(o.lineName);
            if (i != 0) {
                return i;
            }
            i = Integer.compare(lineMachineItem, o.lineMachineItem);
            if (i != 0) {
                return i;
            }
            i = doffingNum.compareTo(o.doffingNum);
            if (i != 0) {
                return i;
            }
            return Integer.compare(spindle, o.spindle);
        }
    }

}
