package com.hengyi.japp.mes.auto.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author jzb 2018-07-30
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SilkCarRecordDTO extends EntityDTO {
    @NotNull
    private EntityDTO silkCar;
}
