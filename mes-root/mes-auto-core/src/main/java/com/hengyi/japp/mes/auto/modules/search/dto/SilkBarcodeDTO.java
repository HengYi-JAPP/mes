package com.hengyi.japp.mes.auto.modules.search.dto;

import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import java.util.Date;

/**
 * @author jzb 2019-02-25
 */
@Data
public class SilkBarcodeDTO extends EntityDTO {
    private LineMachine lineMachine;
    private long codeDoffingNum;
    private Date codeDate;
    private String doffingNum;
    private EntityDTO batch;
    private boolean deleted;

    @Data
    public static class LineMachine extends EntityDTO {
        private Line line;
    }

    @Data
    public static class Line extends EntityDTO {
        private EntityDTO workshop;
    }

}
