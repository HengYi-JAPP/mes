package com.hengyi.japp.mes.auto.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * 丝车，车次
 *
 * @author jzb 2018-06-20
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SilkCarRecordDestination extends LoggableMongoEntity {
    @NotBlank
    private String name;
}