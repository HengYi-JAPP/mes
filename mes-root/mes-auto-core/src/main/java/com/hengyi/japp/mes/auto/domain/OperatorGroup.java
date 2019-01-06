package com.hengyi.japp.mes.auto.domain;

import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import com.hengyi.japp.mes.auto.domain.data.RoleType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;

/**
 * @author jzb 2018-06-22
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class OperatorGroup extends LoggableMongoEntity {
    @ToString.Include
    @NotBlank
    private String name;
    private Collection<RoleType> roles;
    @NotNull
    @Size(min = 1)
    private Collection<Permission> permissions;

}
