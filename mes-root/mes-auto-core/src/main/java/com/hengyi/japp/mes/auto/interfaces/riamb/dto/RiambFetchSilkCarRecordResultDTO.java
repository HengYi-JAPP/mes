package com.hengyi.japp.mes.auto.interfaces.riamb.dto;

import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.domain.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.domain.dto.EntityByCodeDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author jzb 2018-06-22
 */
@Data
public class RiambFetchSilkCarRecordResultDTO implements Serializable {
    /**
     * 是否可以下自动包装线（1:不可以，2:可以）
     */
    private String packeFlage;
    private SilkCarInfo silkCarInfo;
    /**
     * 丝锭数量
     */
    private int silkCount;
    private List<SilkInfo> silkInfos;

    @Data
    public static class SilkCarInfo extends EntityByCodeDTO {
        private int row;
        private int col;
    }

    @Data
    public static class SilkInfo extends CheckSilkDTO {
        /**
         * 抓取标识(1:抓取，2:不抓取)
         */
        private String grabFlage;
        /**
         * 剔除标识(1:不剔除，2:剔除)
         */
        private String eliminateFlage;

        private SilkCarSideType sideType;
        private int row;
        private int col;
        /**
         * 纺位名称，A1-19/3
         */
        private String spec;
        /**
         * 批号
         */
        private String batchNo;
        /**
         * 等级
         */
        private String gradeName;
        private List<String> exceptions;
        private String doffingNum;
        private String doffingOperatorName;
        private DoffingType doffingType;
        private Date doffingDateTime;
        private String otherInfo;
    }

}
