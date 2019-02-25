package com.hengyi.japp.mes.auto.print.config;

import lombok.Data;

import java.io.Serializable;

import static com.github.ixtf.japp.print.Jprint.mmToPix;

/**
 * @author jzb 2018-08-18
 */
@Data
public class BarcodeConfig implements Serializable {
    private float y;
    private float width;
    private float height;

    public double yPix() {
        return mmToPix(y);
    }

    public double getWidthPix() {
        return mmToPix(width);
    }

    public double heightPix() {
        return mmToPix(height);
    }
}
