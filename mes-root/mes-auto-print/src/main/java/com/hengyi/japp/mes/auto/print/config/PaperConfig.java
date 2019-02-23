package com.hengyi.japp.mes.auto.print.config;

import lombok.Data;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.Serializable;

import static com.github.ixtf.japp.print.Jprint.mmToPix;

/**
 * @author jzb 2018-08-18
 */
@Data
public class PaperConfig implements Serializable {
    private float x;
    private float y;
    private float width;
    private float height;
    private int col;
    private float colGap;

    public PageFormat getPageFormat() {
        PageFormat pageFormat = new PageFormat();
        pageFormat.setOrientation(PageFormat.PORTRAIT);
        Paper p = new Paper();
        p.setSize(width, height);
        p.setImageableArea(mmToPix(x), mmToPix(y), mmToPix(width - x * 2), mmToPix(height - y));
        pageFormat.setPaper(p);
        return pageFormat;
    }

    public float getXPix() {
        return mmToPix(x);
    }

    public float getYPix() {
        return mmToPix(y);
    }

    public float getWithPix() {
        return mmToPix(width);
    }

    public float getHeightPix() {
        return mmToPix(height);
    }

    public float getColWith() {
        return (width - (col - 1) * colGap) / col;
    }

    public float getColWithPix() {
        return mmToPix(getColWith());
    }

    public float getColHeight() {
        return height - y;
    }

    public float getColHeightPix() {
        return mmToPix(getColHeight());
    }

    public float getWidthOffSet(int col) {
        return col * (colGap + getColWith());
    }

    public float getWidthOffSetPix(int col) {
        return mmToPix(getWidthOffSet(col));
    }
}
