package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.domain.data.RoleType;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Set;


/**
 * @author jzb 2018-06-21
 */
@Data
public class OperatorGroupUpdateCommand implements Serializable {
    @NotBlank
    private String name;
    private Set<RoleType> roles;
    private Set<EntityDTO> permissions;

}
