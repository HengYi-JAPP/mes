package com.hengyi.japp.mes.auto.interfaces.search.internal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @author jzb 2019-11-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LuceneCommandOne extends LuceneCommandAll {
    @NotBlank
    private String id;
}
