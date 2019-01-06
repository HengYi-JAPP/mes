package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hengyi.japp.mes.auto.application.persistence.annotations.JsonEntityProperty;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import com.hengyi.japp.mes.auto.domain.data.RoleType;
import com.hengyi.japp.mes.auto.interfaces.jackson.ProductEmbedSerializer;
import com.hengyi.japp.mes.auto.interfaces.jackson.SilkExceptionEmbedSerializer;
import com.hengyi.japp.mes.auto.interfaces.jackson.SilkNoteEmbedSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;

/**
 * 产品工序
 *
 * @author jzb 2018-07-03
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class ProductProcess extends LoggableMongoEntity {
    @JsonSerialize(using = ProductEmbedSerializer.class)
    @NotNull
    private Product product;
    @ToString.Include
    @NotBlank
    private String name;
    /**
     * 关联的角色
     */
    @JsonEntityProperty("roles")
    private Collection<RoleType> relateRoles;
    private int sortBy;
    @JsonSerialize(contentUsing = SilkExceptionEmbedSerializer.class)
    @NotNull
    @Size(min = 1)
    private Collection<SilkException> exceptions;
    @JsonSerialize(contentUsing = SilkNoteEmbedSerializer.class)
    @NotNull
    private Collection<SilkNote> notes;
    private FormConfig formConfig;
    private boolean mustProcess;

}
