package com.hengyi.japp.mes.auto.dto;

import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @author jzb 2018-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CheckSilkDTO extends SilkCarPosition {
    @EqualsAndHashCode.Include
    @NotBlank
    private String code;
}
