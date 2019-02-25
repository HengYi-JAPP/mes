package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


/**
 * @author jzb 2018-06-21
 */
@Data
public class BatchUpdateCommand implements Serializable {
    @NotNull
    private EntityDTO workshop;
    @NotNull
    private EntityDTO product;
    @NotBlank
    private String batchNo;
    @Min(1)
    private double silkWeight;
    @Min(1)
    private double centralValue;
    @NotNull
    @Min(1)
    private int holeNum;
    @NotBlank
    private String spec;
    @NotBlank
    private String tubeColor;
    private String note;

}
