package com.hengyi.japp.mes.auto.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * @author jzb 2018-07-29
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PackageClass extends LoggableMongoEntity {
    @ToString.Include
    @NotBlank
    private String name;
    @NotBlank
    private String riambCode;
    private int sortBy;
}
