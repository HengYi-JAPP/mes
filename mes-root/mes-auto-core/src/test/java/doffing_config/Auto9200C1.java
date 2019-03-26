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

import java.util.List;

import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;
import static com.hengyi.japp.mes.auto.config.DoffingSpec.lineMachineSilkSpec;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-03-17
 */
public class Auto9200C1 {
    @SneakyThrows
    public static void main(String[] args) {
        final var doffingSpec = new DoffingSpec();
        doffingSpec.setDoffingType(DoffingType.AUTO);
        doffingSpec.setCorporationCode("9200");
        doffingSpec.setLineName("C1");
        final var silkCarSpec = new SilkCarSpec();
        doffingSpec.setSilkCarSpec(silkCarSpec);
        silkCarSpec.setType(SilkCarType.DEFAULT);
        silkCarSpec.setRow(4);
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

        YAML_MAPPER.writeValue(FileUtils.getFile("/home/mes/auto/doffing_spec", doffingSpec.fileName()), doffingSpec);
    }

    private static List<LineMachineSpec> getLineMachineSpecs() {
        final List<LineMachineSpec> result = Lists.newArrayList();
        result.add(getLineMachineSpec(1, SilkCarSideType.A, 20));
        result.add(getLineMachineSpec(2, SilkCarSideType.B, 20));
        return result;
    }

    private static LineMachineSpec getLineMachineSpec(int orderBy, SilkCarSideType sideType, int spindleNum) {
        final var result = new LineMachineSpec();
        result.setOrderBy(orderBy);
        result.setSpindleNum(spindleNum);
        final List<LineMachineSilkSpec> lineMachineSilkSpecs = Lists.newArrayList();
        result.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 1, 1, 13));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 1, 2, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 1, 3, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 1, 4, 16));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 2, 1, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 2, 2, 15));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 2, 3, 14));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 2, 4, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 3, 1, 17));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 3, 2, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 3, 3, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 3, 4, 12));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 3, 5, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 3, 6, 20));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 4, 1, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 4, 2, 19));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 4, 3, 10));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 4, 4, 11));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 4, 5, 18));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 4, 6, 3));
        return result;
    }
}