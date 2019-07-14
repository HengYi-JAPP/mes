package com.hengyi.japp.mes.auto.domain.data;

import com.google.common.collect.ImmutableList;
import com.hengyi.japp.mes.auto.domain.SilkCar;

import java.util.List;

/**
 * @author jzb 2019-05-25
 */
public enum CarpoolSilkCarModelOrderType {
    //从上往下，从左到右
    DEFAULT {
        @Override
        public List<SilkCarPosition> getOrderedSilkPositions(SilkCar silkCar) {
            final ImmutableList.Builder<SilkCarPosition> builder = ImmutableList.builder();
            final int silkCarRow = silkCar.getRow();
            final int silkCarCol = silkCar.getCol();
            for (SilkCarSideType sideType : SilkCarSideType.values()) {
                for (int silkRow = 1; silkRow <= silkCarRow; silkRow++) {
                    for (int silkCol = 1; silkCol <= silkCarCol; silkCol++) {
                        builder.add(of(sideType, silkRow, silkCol));
                    }
                }
            }
            return builder.build();
        }
    },
    // 右下角开始，S形
    BOTTOM_RIGHT_S {
        @Override
        public List<SilkCarPosition> getOrderedSilkPositions(SilkCar silkCar) {
            final ImmutableList.Builder<SilkCarPosition> builder = ImmutableList.builder();
            final int silkCarRow = silkCar.getRow();
            final int silkCarCol = silkCar.getCol();
            for (SilkCarSideType sideType : SilkCarSideType.values()) {
                int currentRow = 1;
                for (int silkRow = silkCarRow; silkRow >= 1; silkRow--) {
                    if (currentRow % 2 == 1) {
                        for (int silkCol = silkCarCol; silkCol >= 1; silkCol--) {
                            builder.add(of(sideType, silkRow, silkCol));
                        }
                    } else {
                        for (int silkCol = 1; silkCol <= silkCarCol; silkCol++) {
                            builder.add(of(sideType, silkRow, silkCol));
                        }
                    }
                    currentRow++;
                }
            }
            return builder.build();
        }
    };

    private static SilkCarPosition of(SilkCarSideType sideType, int silkRow, int silkCol) {
        final SilkCarPosition silkPosition = new SilkCarPosition();
        silkPosition.setSideType(sideType);
        silkPosition.setRow(silkRow);
        silkPosition.setCol(silkCol);
        return silkPosition;
    }

    public static void main(String[] args) {
        final SilkCar silkCar = new SilkCar();
        silkCar.setRow(4);
        silkCar.setCol(6);
        final List<SilkCarPosition> orderedSilkPositions = BOTTOM_RIGHT_S.getOrderedSilkPositions(silkCar);
        System.out.println(orderedSilkPositions);
    }

    public abstract List<SilkCarPosition> getOrderedSilkPositions(SilkCar silkCar);
}