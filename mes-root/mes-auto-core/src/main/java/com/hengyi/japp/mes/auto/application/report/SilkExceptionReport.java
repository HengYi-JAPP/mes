package com.hengyi.japp.mes.auto.application.report;

import com.hengyi.japp.mes.auto.domain.Silk;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author jzb 2018-11-26
 */
@Data
public class SilkExceptionReport implements Serializable {
    private final Collection<Silk> silks;
}
