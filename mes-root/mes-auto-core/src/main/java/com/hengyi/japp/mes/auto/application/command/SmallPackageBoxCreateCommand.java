package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.dto.SilkCarRecordDTO;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;


/**
 * @author jzb 2018-06-21
 */
@Data
public class SmallPackageBoxCreateCommand implements Serializable {
    @Size(min = 1)
    @NotNull
    private SilkCarRecordDTO silkCarRecord;
    @NotNull
    private SmallPackageBoxConfig config;

    @Data
    public static class SmallPackageBoxConfig implements Serializable {
        @Min(1)
        private int silkCount;
    }
}
