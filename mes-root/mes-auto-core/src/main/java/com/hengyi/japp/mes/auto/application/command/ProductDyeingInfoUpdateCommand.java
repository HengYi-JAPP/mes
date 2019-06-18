package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * @author jzb 2018-06-21
 */
@Data
public class ProductDyeingInfoUpdateCommand implements Serializable {
    private EntityDTO dyeingFormConfig;
    private List<EntityDTO> dyeingExceptions;
    private List<EntityDTO> dyeingNotes;

}
