package com.hengyi.japp.mes.auto.application.command;

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
public class WorkshopUpdateCommand implements Serializable {
    @NotNull
    private EntityDTO corporation;
    @NotBlank
    @Size(min = 1, max = 1)
    private String code;
    @NotBlank
    private String name;
    private String note;
    private Collection<EntityDTO> sapT001ls;
    private Collection<EntityDTO> sapT001lsForeign;
    private Collection<EntityDTO> sapT001lsPallet;

}
