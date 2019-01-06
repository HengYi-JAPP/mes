package com.hengyi.japp.mes.auto.application.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;


/**
 * @author jzb 2018-06-21
 */
@Data
public class SapT001lUpdateCommand implements Serializable {
    @NotBlank
    private String lgort;
    @NotBlank
    private String lgobe;
}
