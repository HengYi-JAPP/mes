package com.hengyi.japp.mes.auto.domain;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 最新染判结果
 *
 * @author jzb 2018-08-02
 */
@Data
public class DyeingResultRuntime implements Serializable {
    @NotNull
    private DyeingResult dyeingResult;
    private Silk silk;
}
