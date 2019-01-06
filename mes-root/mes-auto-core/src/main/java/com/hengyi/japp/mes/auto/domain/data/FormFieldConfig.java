package com.hengyi.japp.mes.auto.domain.data;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * @author jzb 2018-10-25
 */
@Data
public class FormFieldConfig {
    private String id;
    @NotBlank
    private String name;
    @NotNull
    private FormFieldConfig.FormFieldValueType valueType;
    @NotNull
    private FormFieldConfig.FormFieldInputType inputType;
    private boolean required;
    private boolean multi;
    private Collection<String> selectOptions;

    /**
     * 值类型
     */
    public enum FormFieldValueType {
        STRING,
        BOOLEAN,
        NUMBER,
    }

    /**
     * 输入方式
     */
    public enum FormFieldInputType {
        DEFAULT,
        SELECTION,
    }
}
