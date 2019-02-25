package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.domain.data.ProductPlanType;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;


/**
 * @author jzb 2018-06-21
 */
@Data
public class ProductPlanNotifyUpdateCommand implements Serializable {
    @NotNull
    private ProductPlanType type;
    @NotBlank
    private String name;
    @NotNull
    private Date startDate;
    @NotNull
    private EntityDTO batch;
    @NotNull
    @Size(min = 1)
    private Set<EntityDTO> lineMachines;

}
