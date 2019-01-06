package com.hengyi.japp.mes.auto.application;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hengyi.japp.mes.auto.domain.Corporation;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.Workshop;
import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarType;
import com.hengyi.japp.mes.auto.domain.dto.CheckSilkDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.tuple.Triple;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jzb 2018-11-17
 */
@Singleton
public class AutolSilkCarModelConfigRegistry {
    private final Collection<Config> configs;

    @Inject
    private AutolSilkCarModelConfigRegistry(@Named("AutolSilkCarModel.Configs") Collection<Config> configs) {
        this.configs = configs;
    }

    public Config find(SilkCar silkCar, Workshop workshop) {
        final List<Config> finds = configs.stream().filter(it -> it.match(silkCar, workshop)).collect(Collectors.toList());
        if (finds.size() == 1) {
            return finds.get(0);
        }
        throw new RuntimeException("");
    }

    @Data
    public static class Config implements Serializable {
        private String corporationCode;
        private String workshopCode;
        private SilkCarSpec silkCarSpec;
        private List<LineMachineSpec> lineMachineSpecs;

        public List<SilkCarPosition> getOrderedSilkPositions() {
            return lineMachineSpecs.stream()
                    .map(LineMachineSpec::getLineMachineSilkSpecs)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }

        public List<CheckSilkDTO> checkSilks() {
            return lineMachineSpecs.stream()
                    .map(LineMachineSpec::allCheckSilks)
                    .map(list -> {
                        final List<CheckSilkDTO> checkSilks = list.stream().filter(it -> !silkCarSpec.isMiddle(it.getCol())).collect(Collectors.toList());
                        return SilkCarModel.shuffle(checkSilks);
                    })
                    .collect(Collectors.toList());
        }

        public boolean match(SilkCar silkCar, Workshop workshop) {
            final Corporation corporation = workshop.getCorporation();
            if (Objects.equals(workshop.getCode(), workshopCode)
                    && Objects.equals(corporation.getCode(), corporationCode)
                    && Objects.equals(silkCar.getRow(), silkCarSpec.getRow())
                    && Objects.equals(silkCar.getCol(), silkCarSpec.getCol())) {
                return true;
            }
            return false;
        }

        public void selfCheck() {
            final int silkCapacity = lineMachineSpecs.stream()
                    .peek(LineMachineSpec::selfCheck)
                    .mapToInt(LineMachineSpec::getSpindleNum)
                    .sum();
            if (silkCapacity > silkCarSpec.size()) {
                throw new RuntimeException("丝车容量：" + silkCarSpec.size() + "丝锭容量：" + silkCapacity);
            }
            final List<Triple<SilkCarSideType, Integer, Integer>> positionList = lineMachineSpecs.stream()
                    .map(LineMachineSpec::getLineMachineSilkSpecs)
                    .flatMap(Collection::stream)
                    .map(SilkCarPosition::triple)
                    .collect(Collectors.toList());
            final HashSet<Triple<SilkCarSideType, Integer, Integer>> positionSet = Sets.newHashSet(positionList);
            if (positionList.size() != positionSet.size()) {
                throw new RuntimeException("位置重复");
            }
        }
    }

    @Data
    public static class SilkCarSpec implements Serializable {
        private SilkCarType type;
        private int row;
        private int col;

        private boolean isMiddle(int c) {
            final int col = getCol();
            if (col % 2 == 0) {
                return false;
            }
            final int i = col / 2;
            return c == (i + 1);
        }

        private int size() {
            final int size = row * col * 2;
            return SilkCarType.BIG_SILK_CAR == type ? size * 2 : size;
        }
    }

    @Data
    public static class LineMachineSpec implements Serializable {
        private int orderBy;
        private int spindleNum;
        private List<LineMachineSilkSpec> LineMachineSilkSpecs;

        private List<CheckSilkDTO> allCheckSilks() {
            return LineMachineSilkSpecs.stream()
                    .map(LineMachineSilkSpec::toCheckSilk)
                    .collect(Collectors.toList());
        }

        public LineMachineSilkSpec findSilkSpecBySpindle(int spindle) {
            final List<LineMachineSilkSpec> finds = LineMachineSilkSpecs.stream().filter(it -> it.spindle == spindle).collect(Collectors.toList());
            if (finds.size() == 1) {
                return finds.get(0);
            }
            throw new RuntimeException();
        }

        private void selfCheck() {
            final Set<Integer> spindleSet = LineMachineSilkSpecs.stream().map(LineMachineSilkSpec::getSpindle).collect(Collectors.toSet());
            if (spindleSet.size() != spindleNum) {
                throw new RuntimeException("orderBy：" + orderBy + " 锭位号重复");
            }
            if (spindleNum != getLineMachineSilkSpecs().size()) {
                throw new RuntimeException("orderBy：" + orderBy + " 锭数不符");
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
    public static class LineMachineSilkSpec extends SilkCarPosition {
        @EqualsAndHashCode.Include
        private int spindle;

        private CheckSilkDTO toCheckSilk() {
            final CheckSilkDTO checkSilk = new CheckSilkDTO();
            checkSilk.setSideType(sideType);
            checkSilk.setRow(row);
            checkSilk.setCol(col);
            return checkSilk;
        }
    }
}
