package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.domain.data.FormFieldConfig;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;


/**
 * @author jzb 2018-06-21
 */
@Data
public class FormConfigUpdateCommand implements Serializable {
    @NotBlank
    private String name;
    @NotNull
    @Size(min = 1)
    private Collection<FormFieldConfig> formFieldConfigs;

}
