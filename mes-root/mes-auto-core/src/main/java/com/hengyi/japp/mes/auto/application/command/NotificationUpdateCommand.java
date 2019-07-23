package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Collection;


/**
 * @author jzb 2018-06-22
 */
@Data
public class NotificationUpdateCommand implements Serializable {
    private Collection<EntityDTO> workshops;
    private Collection<EntityDTO> lines;
    @NotBlank
    private String note;

}
