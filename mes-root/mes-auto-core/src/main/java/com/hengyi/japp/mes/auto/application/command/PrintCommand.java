package com.hengyi.japp.mes.auto.application.command;

import com.github.ixtf.japp.core.J;
import com.hengyi.japp.mes.auto.domain.data.MesAutoPrinter;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jzb 2018-08-24
 */
@Data
public abstract class PrintCommand implements Serializable {
    public static final Pattern DOFFING_NUM_PATTERN = Pattern.compile("(\\d+$)");
    @NotNull
    private MesAutoPrinter mesAutoPrinter;

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
            if (J.nonBlank(doffingNum) && J.nonBlank(o.doffingNum)) {
                final Matcher m1 = DOFFING_NUM_PATTERN.matcher(doffingNum);
                final Matcher m2 = DOFFING_NUM_PATTERN.matcher(o.doffingNum);
                if (m1.find() && m2.find()) {
                    final Integer i1 = Integer.valueOf(m1.group(0));
                    final Integer i2 = Integer.valueOf(m2.group(0));
                    i = Integer.compare(i1, i2);
                    if (i != 0) {
                        return i;
                    }
                }
            }
            return Integer.compare(spindle, o.spindle);
        }
    }

}
