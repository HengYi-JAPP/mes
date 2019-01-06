package com.hengyi.japp.mes.auto.application.command;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;


@Data
public class TokenCommand implements Serializable {
    @NotBlank
    private String loginId;
    @NotBlank
    private String loginPassword;

}
