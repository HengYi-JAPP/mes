package com.hengyi.japp.mes.auto.application.query;

import lombok.Data;

import java.time.LocalDate;

/**
 * @author jzb 2019-01-04
 */
@Data
public class LocalDateRange {
    private final LocalDate startLd;
    private final LocalDate endLd;
}
