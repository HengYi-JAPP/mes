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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;
import static com.hengyi.japp.mes.auto.config.DoffingSpec.lineMachineSilkSpec;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-03-17
 */
public class Auto9200D2 {
    public static void main(String[] args) {
        Stream.of("D2").forEach(Auto9200D2::doffingSpec);
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
            }).filter(it -> {
                final int col = it.getCol();
                if (col == 3) {
                    return false;
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

    private static List<LineMachineSpec> getLineMachineSpecs() {
        final List<LineMachineSpec> result = Lists.newArrayList();
        result.add(getLineMachineSpec(1, 10));
        result.add(getLineMachineSpec(2, 10));
        result.add(getLineMachineSpec(3, 10));
        return result;
    }

    private static LineMachineSpec getLineMachineSpec(int orderBy, int spindleNum) {
        final var result = new LineMachineSpec();
        result.setOrderBy(orderBy);
        result.setSpindleNum(spindleNum);
        final List<LineMachineSilkSpec> lineMachineSilkSpecs = Lists.newArrayList();
        result.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        IntStream.rangeClosed(1, 5).forEach(col -> {
            lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, orderBy, col, col));
        });
        IntStream.rangeClosed(1, 5).forEach(col -> {
            final int spindle = col + 5;
            lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, orderBy, col, spindle));
        });
        return result;
    }

}