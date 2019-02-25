package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;


/**
 * @author jzb 2018-06-22
 */
@Data
public class LineUpdateCommand implements Serializable {
    @NotNull
    private EntityDTO workshop;
    @NotBlank
    private String name;
    @NotNull
    private DoffingType doffingType;

    @Data
    public static class Batch implements Serializable {
        @NotNull
        @Size(min = 1)
        private Collection<LineUpdateCommand> commands;
    }

}
