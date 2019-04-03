package com.hengyi.japp.mes.auto.config;

import com.google.common.collect.ComparisonChain;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author jzb 2019-03-15
 */
@Data
public class DoffingSpec {
    private DoffingType doffingType;
    private String corporationCode;
    private String lineName;
    private SilkCarSpec silkCarSpec;
    private List<SilkCarPositionCheckSpec> silkCarPositionCheckSpecs;
    private List<List<LineMachineSpec>> lineMachineSpecsList;

    public static LineMachineSilkSpec lineMachineSilkSpec(SilkCarSideType sideType, int row, int col, int spindle) {
        var result = new LineMachineSilkSpec();
        result.setSideType(sideType);
        result.setRow(row);
        result.setCol(col);
        result.setSpindle(spindle);
        return result;
    }

    public String fileName() {
        final int row = silkCarSpec.getRow();
        final int col = silkCarSpec.getCol();
        final String s = String.join("_", corporationCode, lineName, row + "X" + col, doffingType.name());
        return s + ".yml";
    }

    @Data
    public static class SilkCarSpec implements Serializable {
        private SilkCarType type;
        private int row;
        private int col;
    }

    @Data
    public static class SilkCarPositionCheckSpec implements Serializable, Comparable<SilkCarPositionCheckSpec> {
        private int orderBy;
        private List<SilkCarPosition> silkCarPositions;

        @Override
        public int compareTo(SilkCarPositionCheckSpec o) {
            return ComparisonChain.start()
                    .compare(orderBy, o.orderBy)
                    .result();
        }
    }

    @Data
    public static class LineMachineSpec implements Serializable, Comparable<LineMachineSpec> {
        private int orderBy;
        private int spindleNum;
        private List<LineMachineSilkSpec> lineMachineSilkSpecs;

        @Override
        public int compareTo(LineMachineSpec o) {
            return ComparisonChain.start()
                    .compare(orderBy, o.orderBy)
                    .result();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class LineMachineSilkSpec extends SilkCarPosition {
        @EqualsAndHashCode.Include
        private int spindle;
    }
}
