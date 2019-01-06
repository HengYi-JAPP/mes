package com.hengyi.japp.mes.auto.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author jzb 2018-06-22
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EntityDTO implements Serializable {
    @EqualsAndHashCode.Include
    @NotBlank
    private String id;
}
