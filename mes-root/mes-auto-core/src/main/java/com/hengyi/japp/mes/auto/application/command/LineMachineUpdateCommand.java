package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;


/**
 * @author jzb 2018-06-22
 */
@Data
public class LineMachineUpdateCommand implements Serializable {
    @NotNull
    private EntityDTO line;
    @Min(1)
    private int item;
    @Min(1)
    private int spindleNum;
    @NotNull
    @Size(min = 1)
    private List<Integer> spindleSeq;

    @Data
    public static class Batch implements Serializable {
        @NotNull
        @Size(min = 1)
        private Collection<LineMachineUpdateCommand> commands;
    }
}
