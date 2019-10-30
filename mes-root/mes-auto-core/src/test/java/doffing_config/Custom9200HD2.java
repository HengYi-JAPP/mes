package doffing_config;

import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.config.DoffingSpec;
import com.hengyi.japp.mes.auto.config.DoffingSpec.LineMachineSilkSpec;
import com.hengyi.japp.mes.auto.config.DoffingSpec.LineMachineSpec;
import com.hengyi.japp.mes.auto.config.DoffingSpec.SilkCarPositionCheckSpec;
import com.hengyi.japp.mes.auto.config.DoffingSpec.SilkCarSpec;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.domain.data.SilkCarPosition;
import com.hengyi.japp.mes.auto.domain.data.SilkCarType;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;
import static com.hengyi.japp.mes.auto.config.DoffingSpec.lineMachineSilkSpec;
import static com.hengyi.japp.mes.auto.domain.data.SilkCarSideType.A;
import static com.hengyi.japp.mes.auto.domain.data.SilkCarSideType.B;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-03-17
 */
public class Custom9200HD2 {
    public static void main(String[] args) {
        Stream.of("HD2").forEach(Custom9200HD2::doffingSpec3X6);
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
        lineMachineSpecsList.add(getLineMachineSpecs());

        final var silkCarPositionCheckSpecs = lineMachineSpecsList.get(0).stream().map(lineMachineSpec -> {
            final var silkCarPositionCheckSpec = new SilkCarPositionCheckSpec();
            silkCarPositionCheckSpec.setOrderBy(lineMachineSpec.getOrderBy());
            final List<SilkCarPosition> silkCarPositions = lineMachineSpec.getLineMachineSilkSpecs().stream().map(silkSpec -> {
                final SilkCarPosition silkCarPosition = new SilkCarPosition();
                silkCarPosition.setSideType(silkSpec.getSideType());
                silkCarPosition.setRow(silkSpec.getRow());
                silkCarPosition.setCol(silkSpec.getCol());
                return silkCarPosition;
            }).collect(toList());
            silkCarPositionCheckSpec.setSilkCarPositions(silkCarPositions);
            return silkCarPositionCheckSpec;
        }).collect(toList());
        doffingSpec.setSilkCarPositionCheckSpecs(silkCarPositionCheckSpecs);

        final File dir = FileUtils.getFile("/home/mes/auto/doffing_spec", doffingSpec.fileName());
        FileUtils.forceMkdirParent(dir);
        YAML_MAPPER.writeValue(dir, doffingSpec);
        return doffingSpec;
    }

    private static List<LineMachineSpec> getLineMachineSpecs() {
        final List<LineMachineSpec> result = Lists.newArrayList();

        LineMachineSpec lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setSpindleNum(5);
        lineMachineSpec.setOrderBy(1);
        List<LineMachineSilkSpec> lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 3, 6, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 3, 5, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 2, 5, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 2, 6, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 1, 6, 5));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setSpindleNum(5);
        lineMachineSpec.setOrderBy(2);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 3, 4, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 3, 3, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 2, 3, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 2, 4, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 1, 4, 5));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setSpindleNum(5);
        lineMachineSpec.setOrderBy(3);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 3, 2, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 3, 1, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 2, 1, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 2, 2, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(A, 1, 2, 5));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setSpindleNum(5);
        lineMachineSpec.setOrderBy(4);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 3, 6, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 3, 5, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 2, 5, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 2, 6, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 1, 6, 5));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setSpindleNum(5);
        lineMachineSpec.setOrderBy(5);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 3, 4, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 3, 3, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 2, 3, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 2, 4, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 1, 4, 5));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setSpindleNum(5);
        lineMachineSpec.setOrderBy(6);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 3, 2, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 3, 1, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 2, 1, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 2, 2, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(B, 1, 2, 5));
        return result;
    }

}