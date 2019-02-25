package com.hengyi.japp.mes.auto.domain.data;

import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @author jzb 2018-11-22
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class MesAutoPrinter extends EntityDTO {
    @NotBlank
    private String name;
}
