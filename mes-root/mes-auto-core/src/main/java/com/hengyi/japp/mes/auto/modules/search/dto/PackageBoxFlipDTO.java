package com.hengyi.japp.mes.auto.modules.search.dto;

import com.hengyi.japp.mes.auto.domain.data.PackageBoxFlipType;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import java.util.Collection;
import java.util.Date;

/**
 * @author jzb 2019-02-25
 */
@Data
public class PackageBoxFlipDTO extends EntityDTO {
    private PackageBoxFlipType type;
    private PackageBoxInfo packageBox;
    private Collection<SilkInfo> inSilks;
    private Collection<SilkInfo> outSilks;
    private OperatorDTO creator;
    private Date createDateTime;
    private OperatorDTO modifier;
    private Date modifyDateTime;

    @Data
    public static class PackageBoxInfo extends EntityDTO {
        private BatchInfo batch;
        private EntityDTO grade;
        private Date budat;
        private EntityDTO budatClass;
        private EntityDTO sapT001l;
    }

    @Data
    public static class BatchInfo extends EntityDTO {
        private EntityDTO product;
        private EntityDTO workshop;
    }

    @Data
    public static class SilkInfo extends EntityDTO {
        private String code;
    }
}
