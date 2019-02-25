package com.hengyi.japp.mes.auto.interfaces.riamb.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author jzb 2018-06-22
 */
@Data
public class RiambPackageBoxEventDTO implements Serializable {
    @NotBlank
    private String code;
    @NotNull
    private AutomaticPackeJobInfo jobInfo;
    @Min(1)
    @NotNull
    private BigDecimal netWeight;
    @Min(1)
    @NotNull
    private BigDecimal grossWeight;
    @Min(1)
    private int silkCount;
    @NotNull
    private Date createDateTime;
    @Size(min = 1)
    @NotNull
    private List<SilkInfo> silkInfos;
    private String palletCode;
    private String otherInfo;

    @Data
    public static class AutomaticPackeJobInfo implements Serializable {
        @NotBlank
        private String id;
        @NotBlank
        private String automaticPackeLine;
        @NotBlank
        private String batchNo;
        @NotBlank
        private String gradeName;
        private String packageClassNo;
        private String saleType;
        private Date budatDate;
        private String palletType;
        private String packageType;
        private String foamType;
        private int foamNum;
        @NotNull
        private String creatorHrId;
        @NotNull
        private Date createDateTime;
        private String lgort;
        private String otherInfo;
    }

    @Data
    public static class SilkInfo implements Serializable {
        @NotBlank
        private String code;
        private BigDecimal weight;
    }

}
