package com.hengyi.japp.mes.auto.interfaces.warehouse;

import com.hengyi.japp.mes.auto.application.command.PackageBoxMeasureInfoUpdateCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @author jzb 2019-12-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class WarehouseMeasureInfoUpdateCommand extends PackageBoxMeasureInfoUpdateCommand {
    @NotBlank
    private String id;
}
