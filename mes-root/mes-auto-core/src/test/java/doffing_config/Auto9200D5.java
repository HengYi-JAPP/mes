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
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;
import static com.hengyi.japp.mes.auto.config.DoffingSpec.lineMachineSilkSpec;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-03-17
 */
public class Auto9200D5 {
    public static void main(String[] args) {
        Stream.of("D5").forEach(Auto9200D5::doffingSpec);
    }

    @SneakyThrows
    private static DoffingSpec doffingSpec(String lineName) {
        final var doffingSpec = new DoffingSpec();
        doffingSpec.setDoffingType(DoffingType.AUTO);
        doffingSpec.setCorporationCode("9200");
        doffingSpec.setLineName(lineName);
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
        return doffingSpec;
    }

    private static List<LineMachineSpec> getLineMachineSpecs() {
        final List<LineMachineSpec> result = Lists.newArrayList();
        result.add(getLineMachineSpec(1, SilkCarSideType.A, 24));
        result.add(getLineMachineSpec(2, SilkCarSideType.B, 24));
        return result;
    }

    private static LineMachineSpec getLineMachineSpec(int orderBy, SilkCarSideType sideType, int spindleNum) {
        final var result = new LineMachineSpec();
        result.setOrderBy(orderBy);
        result.setSpindleNum(spindleNum);
        final List<LineMachineSilkSpec> lineMachineSilkSpecs = Lists.newArrayList();
        result.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 1, 1, 13));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 1, 2, 1));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 1, 3, 17));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 1, 4, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 1, 5, 21));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 1, 6, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 2, 1, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 2, 2, 14));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 2, 3, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 2, 4, 18));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 2, 5, 10));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 2, 6, 22));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 3, 1, 15));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 3, 2, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 3, 3, 19));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 3, 4, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 3, 5, 23));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 3, 6, 11));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 4, 1, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 4, 2, 16));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 4, 3, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 4, 4, 20));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 4, 5, 12));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(sideType, 4, 6, 24));
        return result;
    }

}