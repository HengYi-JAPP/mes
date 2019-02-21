package com.hengyi.japp.mes.auto.interfaces.riamb.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @author jzb 2018-06-22
 */
@Data
public class RiambSilkDetachEventDTO implements Serializable {
    @NotBlank
    private String silkCarCode;
    @NotBlank
    private List<String> silkCodes;

}
