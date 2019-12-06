package com.hengyi.japp.mes.auto.interfaces.search.internal;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author jzb 2019-11-14
 */
@Data
public class LuceneCommandAll implements Serializable {
    @NotBlank
    private String className;

    public void setClassName(Class<?> entityClass) {
        this.className = entityClass.getName();
    }
}
