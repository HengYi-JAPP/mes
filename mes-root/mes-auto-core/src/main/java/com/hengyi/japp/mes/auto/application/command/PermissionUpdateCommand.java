package com.hengyi.japp.mes.auto.application.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;


/**
 * @author jzb 2018-06-22
 */
@Data
public class PermissionUpdateCommand implements Serializable {
    @NotBlank
    private String name;
    @NotBlank
    private String code;

}
