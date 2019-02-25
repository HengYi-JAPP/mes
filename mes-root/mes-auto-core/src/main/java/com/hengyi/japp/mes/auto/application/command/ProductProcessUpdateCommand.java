package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.domain.data.RoleType;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Set;


/**
 * @author jzb 2018-06-21
 */
@Data
public class ProductProcessUpdateCommand implements Serializable {
    @NotNull
    private EntityDTO product;
    private Set<RoleType> relateRoles;
    @NotNull
    @Size(min = 1)
    private List<EntityDTO> exceptions;
    private List<EntityDTO> notes;
    @NotBlank
    private String name;
    private int sortBy;
    private EntityDTO formConfig;
    private boolean mustProcess;

}
