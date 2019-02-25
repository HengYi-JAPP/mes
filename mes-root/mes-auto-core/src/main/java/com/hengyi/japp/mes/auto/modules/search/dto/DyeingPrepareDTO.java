package com.hengyi.japp.mes.auto.modules.search.dto;

import com.hengyi.japp.mes.auto.domain.data.DyeingType;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author jzb 2019-02-25
 */
@Data
public class DyeingPrepareDTO extends EntityDTO {
    private DyeingType type;
    private SilkCarRecordInfo silkCarRecord;
    private Collection<SilkInfo> silks;
    private List<SilkCarRecordInfo> silkCarRecords;
    private boolean submitted;
    private OperatorDTO creator;
    private Date createDateTime;
    private OperatorDTO submitter;
    private Date submitDateTime;

    @Data
    public static class SilkCarRecordInfo extends EntityDTO {
        private SilkCarInfo silkCar;
        private BatchInfo batch;
    }

    @Data
    public static class SilkCarInfo extends EntityDTO {
        private String code;
    }

    @Data
    public static class SilkInfo extends EntityDTO {
        private String doffingNum;
        private BatchInfo batch;
        private EntityDTO lineMachine;
    }

    @Data
    public static class BatchInfo extends EntityDTO {
        private WorkshopInfo workshop;
    }

    @Data
    public static class WorkshopInfo extends EntityDTO {
    }
}
