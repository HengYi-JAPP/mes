package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.domain.data.RoleType;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;


/**
 * @author jzb 2018-06-22
 */
@Data
public class OperatorPermissionUpdateCommand implements Serializable {
    private boolean admin;
    private Set<RoleType> roles;
    private Set<EntityDTO> groups;
    private Set<EntityDTO> permissions;

}
