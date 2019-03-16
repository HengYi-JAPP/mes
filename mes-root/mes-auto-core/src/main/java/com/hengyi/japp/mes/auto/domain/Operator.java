package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hengyi.japp.mes.auto.application.persistence.MongoEntity;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import com.hengyi.japp.mes.auto.domain.data.RoleType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;

/**
 * @author jzb 2018-06-22
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class Operator extends MongoEntity {
    @ToString.Include
    private String name;
    private String hrId;
    private String oaId;
    private String phone;
    private boolean admin;
    @JsonIgnore
    private Collection<OperatorGroup> groups;
    @JsonIgnore
    private Collection<RoleType> roles;
    @JsonIgnore
    private Collection<Permission> permissions;
}
