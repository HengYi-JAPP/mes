package com.hengyi.japp.mes.auto.application.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;


/**
 * @author liuyuan 2018-03-28
 */
@Data
public class OperatorUpdateCommand implements Serializable {
    @NotBlank
    private String hrId;
    private String oaId;
    @NotBlank
    private String name;

}
