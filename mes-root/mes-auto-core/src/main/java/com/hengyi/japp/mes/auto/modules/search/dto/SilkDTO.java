package com.hengyi.japp.mes.auto.modules.search.dto;

import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author jzb 2019-02-25
 */
@Data
public class SilkDTO extends EntityDTO {
    private String code;
    private LineMachineInfo lineMachine;
    private int spindle;
    private EntityDTO batch;
    private EntityDTO grade;
    private DoffingType doffingType;
    private Date doffingDateTime;
    private boolean dyeingSample;
    private boolean detached;
    private List<EntityDTO> silkCarRecords;
    private EntityDTO temporaryBox;
    private EntityDTO packageBox;
    private Date packageDateTime;

    @Data
    public static class LineMachineInfo extends EntityDTO {
        private LineInfo line;
    }

    @Data
    public static class LineInfo extends EntityDTO {
        private WorkshopInfo workshop;
    }

    @Data
    public static class WorkshopInfo extends EntityDTO {
        private EntityDTO corporation;
    }
}
