package com.hengyi.japp.mes.auto.domain.data;

import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Triple;

import java.io.Serializable;

/**
 * @author jzb 2018-11-15
 */
@Data
public class SilkCarPosition implements Serializable {
    @ToString.Include
    protected SilkCarSideType sideType;
    @ToString.Include
    protected int row;
    @ToString.Include
    protected int col;

    public Triple<SilkCarSideType, Integer, Integer> triple() {
        return Triple.of(sideType, row, col);
    }
}
