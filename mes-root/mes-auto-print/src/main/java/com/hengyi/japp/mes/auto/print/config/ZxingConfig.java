package com.hengyi.japp.mes.auto.print.config;

import lombok.Data;

import java.io.Serializable;

/**
 * @author jzb 2018-08-18
 */
@Data
public class ZxingConfig implements Serializable {
    private int width;
    private int height;
}
