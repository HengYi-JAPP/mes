package com.hengyi.japp.mes.auto.application.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;


/**
 * @author jzb 2018-06-22
 */
@Data
public class PasswordChangeCommand implements Serializable {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String newPasswordAgain;

}
