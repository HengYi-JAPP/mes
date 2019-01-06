package com.hengyi.japp.mes.auto.application.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;


/**
 * @author jzb 2018-06-21
 */
@Data
public class ProductUpdateCommand implements Serializable {
    @NotBlank
    private String name;
    @NotBlank
    @Size(min = 2, max = 2)
    private String code;

}
