package doffing_config;

import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.config.DoffingSpec;
import com.hengyi.japp.mes.auto.config.DoffingSpec.LineMachineSilkSpec;
import com.hengyi.japp.mes.auto.config.DoffingSpec.LineMachineSpec;
import com.hengyi.japp.mes.auto.config.DoffingSpec.SilkCarPositionCheckSpec;
import com.hengyi.japp.mes.auto.config.DoffingSpec.SilkCarSpec;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarType;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;
import static com.hengyi.japp.mes.auto.config.DoffingSpec.lineMachineSilkSpec;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-03-17
 */
public class Custom9200D1_2 {
    public static void main(String[] args) {
        Stream.of("D1", "D2").forEach(it -> {
            doffingSpec3X6(it);
            doffingSpec4X4(it);
            doffingSpec4X5(it);
        });
    }

    @SneakyThrows
    private static DoffingSpec doffingSpec3X6(String lineName) {
        final var doffingSpec = new DoffingSpec();
        doffingSpec.setDoffingType(DoffingType.MANUAL);
        doffingSpec.setCorporationCode("9200");
        doffingSpec.setLineName(lineName);
        final var silkCarSpec = new SilkCarSpec();
        doffingSpec.setSilkCarSpec(silkCarSpec);
        silkCarSpec.setType(SilkCarType.DEFAULT);
        silkCarSpec.setRow(3);
        silkCarSpec.setCol(6);

        final List<List<LineMachineSpec>> lineMachineSpecsList = Lists.newArrayList();
        doffingSpec.setLineMachineSpecsList(lineMachineSpecsList);
        lineMachineSpecsList.add(getLineMachineSpecs3X6_1());
        lineMachineSpecsList.add(getLineMachineSpecs3X6_2());

        final var silkCarPositionCheckSpecs = lineMachineSpecsList.get(0).stream().map(lineMachineSpec -> {
            final var silkCarPositionCheckSpec = new SilkCarPositionCheckSpec();
            silkCarPositionCheckSpec.setOrderBy(lineMachineSpec.getOrderBy());
            final List<SilkCarPosition> silkCarPositions = lineMachineSpec.getLineMachineSilkSpecs().stream().map(silkSpec -> {
                final SilkCarPosition silkCarPosition = new SilkCarPosition();
                silkCarPosition.setSideType(silkSpec.getSideType());
                silkCarPosition.setRow(silkSpec.getRow());
                silkCarPosition.setCol(silkSpec.getCol());
                return silkCarPosition;
            }).filter(it -> {
                final SilkCarSideType sideType = it.getSideType();
                final int row = it.getRow();
                final int col = it.getCol();
                if (SilkCarSideType.A == sideType) {
                    if (2 == row || 3 == row) {
                        if (2 == col || 5 == col) {
                            return false;
                        }
                    }
                } else {
                    if (2 == row || 3 == row) {
                        if (5 == col) {
                            return false;
                        }
                    }
                }
                return true;
            }).collect(toList());
            silkCarPositionCheckSpec.setSilkCarPositions(silkCarPositions);
            return silkCarPositionCheckSpec;
        }).collect(toList());
        doffingSpec.setSilkCarPositionCheckSpecs(silkCarPositionCheckSpecs);

        final File dir = FileUtils.getFile("/home/mes/auto/doffing_spec_config", doffingSpec.fileName());
        dir.mkdir();
        YAML_MAPPER.writeValue(dir, doffingSpec);
        return doffingSpec;
    }

    private static List<LineMachineSpec> getLineMachineSpecs3X6_1() {
        final List<LineMachineSpec> result = Lists.newArrayList();
        var lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(1);
        lineMachineSpec.setSpindleNum(10);
        List<LineMachineSilkSpec> lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 3, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 4, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 5, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 6, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 4, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 5, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 6, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 4, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 5, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 6, 10));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(2);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 1, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 2, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 1, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 2, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 3, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 1, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 2, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 3, 10));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 6, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 6, 2));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(3);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 2, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 3, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 4, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 5, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 3, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 4, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 5, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 3, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 4, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 5, 10));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(4);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 1, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 1, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 2, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 1, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 2, 5));
        return result;
    }

    private static List<LineMachineSpec> getLineMachineSpecs3X6_2() {
        final List<LineMachineSpec> result = Lists.newArrayList();
        var lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(1);
        lineMachineSpec.setSpindleNum(10);
        List<LineMachineSilkSpec> lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 3, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 4, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 5, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 6, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 4, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 5, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 6, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 4, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 5, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 6, 10));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(2);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 1, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 2, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 1, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 2, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 3, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 1, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 2, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 3, 10));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 6, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 6, 2));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(3);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 2, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 3, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 4, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 5, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 3, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 4, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 5, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 3, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 4, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 5, 10));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(4);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 1, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 1, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 2, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 1, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 2, 10));
        return result;
    }

    @SneakyThrows
    private static DoffingSpec doffingSpec4X4(String lineName) {
        final var doffingSpec = new DoffingSpec();
        doffingSpec.setDoffingType(DoffingType.MANUAL);
        doffingSpec.setCorporationCode("9200");
        doffingSpec.setLineName(lineName);
        final var silkCarSpec = new SilkCarSpec();
        doffingSpec.setSilkCarSpec(silkCarSpec);
        silkCarSpec.setType(SilkCarType.DEFAULT);
        silkCarSpec.setRow(4);
        silkCarSpec.setCol(4);

        final List<List<LineMachineSpec>> lineMachineSpecsList = Lists.newArrayList();
        doffingSpec.setLineMachineSpecsList(lineMachineSpecsList);
        lineMachineSpecsList.add(getLineMachineSpecs4X4());

        final var silkCarPositionCheckSpecs = lineMachineSpecsList.get(0).stream().map(lineMachineSpec -> {
            final var silkCarPositionCheckSpec = new SilkCarPositionCheckSpec();
            silkCarPositionCheckSpec.setOrderBy(lineMachineSpec.getOrderBy());
            final List<SilkCarPosition> silkCarPositions = lineMachineSpec.getLineMachineSilkSpecs().stream().map(silkSpec -> {
                final SilkCarPosition silkCarPosition = new SilkCarPosition();
                silkCarPosition.setSideType(silkSpec.getSideType());
                silkCarPosition.setRow(silkSpec.getRow());
                silkCarPosition.setCol(silkSpec.getCol());
                return silkCarPosition;
            }).filter(it -> {
                final int row = it.getRow();
                final int col = it.getCol();
                if (1 == row || 3 == row) {
                    if (3 == col) {
                        return false;
                    }
                }
                return true;
            }).collect(toList());
            silkCarPositionCheckSpec.setSilkCarPositions(silkCarPositions);
            return silkCarPositionCheckSpec;
        }).collect(toList());
        doffingSpec.setSilkCarPositionCheckSpecs(silkCarPositionCheckSpecs);

        YAML_MAPPER.writeValue(FileUtils.getFile("/home/mes/auto/doffing_spec", doffingSpec.fileName()), doffingSpec);
        return doffingSpec;
    }

    private static List<LineMachineSpec> getLineMachineSpecs4X4() {
        final List<LineMachineSpec> result = Lists.newArrayList();
        var lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(1);
        lineMachineSpec.setSpindleNum(10);
        List<LineMachineSilkSpec> lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 2, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 3, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 4, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 3, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 4, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 2, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 3, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 4, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 3, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 4, 10));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(2);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 1, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 2, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 1, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 1, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 2, 10));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 1, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 2, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 1, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 1, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 2, 5));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(3);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 2, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 3, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 4, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 3, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 4, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 2, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 3, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 4, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 3, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 4, 10));
        return result;
    }

    @SneakyThrows
    private static DoffingSpec doffingSpec4X5(String lineName) {
        final var doffingSpec = new DoffingSpec();
        doffingSpec.setDoffingType(DoffingType.MANUAL);
        doffingSpec.setCorporationCode("9200");
        doffingSpec.setLineName(lineName);
        final var silkCarSpec = new SilkCarSpec();
        doffingSpec.setSilkCarSpec(silkCarSpec);
        silkCarSpec.setType(SilkCarType.DEFAULT);
        silkCarSpec.setRow(4);
        silkCarSpec.setCol(5);

        final List<List<LineMachineSpec>> lineMachineSpecsList = Lists.newArrayList();
        doffingSpec.setLineMachineSpecsList(lineMachineSpecsList);
        lineMachineSpecsList.add(getLineMachineSpecs4X5_1());
        lineMachineSpecsList.add(getLineMachineSpecs4X5_2());

        final var silkCarPositionCheckSpecs = lineMachineSpecsList.get(0).stream().map(lineMachineSpec -> {
            final var silkCarPositionCheckSpec = new SilkCarPositionCheckSpec();
            silkCarPositionCheckSpec.setOrderBy(lineMachineSpec.getOrderBy());
            final List<SilkCarPosition> silkCarPositions = lineMachineSpec.getLineMachineSilkSpecs().stream().map(silkSpec -> {
                final SilkCarPosition silkCarPosition = new SilkCarPosition();
                silkCarPosition.setSideType(silkSpec.getSideType());
                silkCarPosition.setRow(silkSpec.getRow());
                silkCarPosition.setCol(silkSpec.getCol());
                return silkCarPosition;
            }).filter(it -> {
                final SilkCarSideType sideType = it.getSideType();
                final int row = it.getRow();
                final int col = it.getCol();
                if (SilkCarSideType.A == sideType) {
                    if (1 == row || 3 == row) {
                        if (4 == col) {
                            return false;
                        }
                    }
                } else {
                    if (2 == row || 4 == row) {
                        if (3 == col) {
                            return false;
                        }
                    }
                }
                return true;
            }).collect(toList());
            silkCarPositionCheckSpec.setSilkCarPositions(silkCarPositions);
            return silkCarPositionCheckSpec;
        }).collect(toList());
        doffingSpec.setSilkCarPositionCheckSpecs(silkCarPositionCheckSpecs);

        YAML_MAPPER.writeValue(FileUtils.getFile("/home/mes/auto/auto_doffing_config", doffingSpec.fileName()), doffingSpec);
        return doffingSpec;
    }

    private static List<LineMachineSpec> getLineMachineSpecs4X5_1() {
        final List<LineMachineSpec> result = Lists.newArrayList();
        var lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(1);
        lineMachineSpec.setSpindleNum(10);
        List<LineMachineSilkSpec> lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 3, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 4, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 5, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 3, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 4, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 3, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 4, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 5, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 3, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 4, 10));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(2);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 1, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 2, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 1, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 2, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 1, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 2, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 1, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 2, 10));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 5, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 5, 2));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(3);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 3, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 4, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 2, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 3, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 4, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 3, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 4, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 2, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 3, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 4, 10));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(4);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 1, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 2, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 1, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 1, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 2, 5));
        return result;
    }

    private static List<LineMachineSpec> getLineMachineSpecs4X5_2() {
        final List<LineMachineSpec> result = Lists.newArrayList();
        var lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(1);
        lineMachineSpec.setSpindleNum(10);
        List<LineMachineSilkSpec> lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 3, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 4, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 5, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 3, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 4, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 3, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 4, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 5, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 3, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 4, 10));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(2);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 1, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 2, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 1, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 2, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 1, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 2, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 1, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 2, 10));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 5, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 5, 2));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(3);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 3, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 4, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 2, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 3, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 4, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 3, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 4, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 2, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 3, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 4, 10));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(4);
        lineMachineSpec.setSpindleNum(10);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 1, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 2, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 1, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 1, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 2, 10));
        return result;
    }

}