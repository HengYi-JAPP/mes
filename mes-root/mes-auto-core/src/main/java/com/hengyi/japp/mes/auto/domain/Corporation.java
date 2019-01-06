package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hengyi.japp.mes.auto.application.persistence.annotations.MongoCache;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 公司
 *
 * @author jzb 2018-06-21
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@MongoCache
public class Corporation extends LoggableMongoEntity {
    /**
     * 编码，最好和 SAP 编码统一
     */
    @ToString.Include
    @NotBlank
    private String code;
    @JsonIgnore
    @NotBlank
    @Size(min = 2, max = 2)
    private String packageCode;
    @ToString.Include
    @NotBlank
    private String name;

}
