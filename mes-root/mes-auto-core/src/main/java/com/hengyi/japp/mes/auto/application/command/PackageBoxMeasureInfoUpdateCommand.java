package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.domain.data.SaleType;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;


/**
 * @author jzb 2018-06-21
 */
@Data
public class PackageBoxMeasureInfoUpdateCommand implements Serializable {
    @NotNull
    private SaleType saleType;
    @Min(1)
    private int silkCount;
    @Min(1)
    private double grossWeight;
    @Min(1)
    private double netWeight;
    @NotNull
    private Date budat;
    @NotNull
    private EntityDTO budatClass;
    @NotNull
    private EntityDTO sapT001l;
    @NotBlank
    private String palletType;
    @NotBlank
    private String packageType;
    @NotBlank
    private String foamType;
    @Min(0)
    private int foamNum;
    private String palletCode;
}
