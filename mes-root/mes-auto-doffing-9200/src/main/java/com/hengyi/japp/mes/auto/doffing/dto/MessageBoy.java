package com.hengyi.japp.mes.auto.doffing.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * @author jzb 2019-03-09
 */
@Data
public class MessageBoy implements Serializable {
    private String id;
    private String line;
    private String principalName;
    private Date createDateTime;
    private SilkCarInfo silkCarInfo;
    private Collection<SilkInfo> silkInfos;

    @Data
    public static class SilkCarInfo implements Serializable {
        private String code;
        private int row;
        private int col;
        private String batchNo;
        private String batchSpec;
        private String grade;
    }

    /**
     * { "line":"C5", "lineMachine":47, "spindle":1, "timestamp":1552038447 }
     */
    @Data
    public static class SilkInfo implements Serializable {
        private String sideType;
        private int row;
        private int col;
        private String line;
        private int lineMachine;
        private int spindle;
        private long timestamp;

        @JsonGetter
        public Date getDoffingDateTime() {
            final long l = timestamp * 1000;
            return new Date(l);
        }
    }
}
