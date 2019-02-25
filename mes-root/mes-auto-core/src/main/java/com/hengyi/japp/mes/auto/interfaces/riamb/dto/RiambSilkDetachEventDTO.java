package com.hengyi.japp.mes.auto.interfaces.riamb.dto;

import com.hengyi.japp.mes.auto.dto.EntityDTO;
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
    private SilkCarInfo silkCarInfo;
    @NotBlank
    private List<String> silkCodes;

    @Data
    public static class SilkCarInfo extends EntityDTO {
        @NotBlank
        private String code;
    }

}
