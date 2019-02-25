package com.hengyi.japp.mes.auto.modules.search.dto;

import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import java.util.Date;

/**
 * @author jzb 2019-02-25
 */
@Data
public class SilkCarRecordDTO extends EntityDTO {
    private SilkCar silkCar;
    private Batch batch;
    private Date startDateTime;
    private Date endDateTime;

    @Data
    public static class SilkCar extends EntityDTO {
        private String code;
    }

    @Data
    public static class Batch extends EntityDTO {
        private EntityDTO product;
        private EntityDTO workshop;
    }

}
