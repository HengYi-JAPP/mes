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
public class Custom9200C2_3 {
    public static void main(String[] args) {
        Stream.of("C2", "C3").forEach(Custom9200C2_3::doffingSpec);
    }

    @SneakyThrows
    private static DoffingSpec doffingSpec(String lineName) {
        final var doffingSpec = new DoffingSpec();
        doffingSpec.setDoffingType(DoffingType.MANUAL);
        doffingSpec.setCorporationCode("9200");
        doffingSpec.setLineName(lineName);
        final var silkCarSpec = new SilkCarSpec();
        doffingSpec.setSilkCarSpec(silkCarSpec);
        silkCarSpec.setType(SilkCarType.DEFAULT);
        silkCarSpec.setRow(4);
        silkCarSpec.setCol(6);

        final List<List<LineMachineSpec>> lineMachineSpecsList = Lists.newArrayList();
        doffingSpec.setLineMachineSpecsList(lineMachineSpecsList);
        lineMachineSpecsList.add(getLineMachineSpecs1());
        lineMachineSpecsList.add(getLineMachineSpecs2());

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

        YAML_MAPPER.writeValue(FileUtils.getFile("/home/mes/auto/auto_doffing_config", doffingSpec.fileName()), doffingSpec);
        return doffingSpec;
    }

    private static List<LineMachineSpec> getLineMachineSpecs1() {
        final List<LineMachineSpec> result = Lists.newArrayList();
        var lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(1);
        lineMachineSpec.setSpindleNum(32);
        List<LineMachineSilkSpec> lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 1, 14));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 2, 13));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 3, 12));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 4, 11));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 5, 10));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 6, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 1, 15));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 2, 16));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 3, 32));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 4, 31));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 5, 30));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 6, 29));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 1, 23));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 2, 24));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 3, 25));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 4, 26));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 5, 27));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 6, 28));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 1, 22));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 2, 21));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 3, 20));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 4, 19));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 5, 18));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 6, 17));

        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 1, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 2, 1));

        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 1, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 2, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 3, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 4, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 5, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 6, 8));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(2);
        lineMachineSpec.setSpindleNum(32);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 1, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 2, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 3, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 4, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 5, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 6, 1));

        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 1, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 2, 8));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 3, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 4, 10));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 5, 11));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 6, 12));

        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 3, 16));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 4, 15));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 5, 14));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 6, 13));
        return result;
    }

    private static List<LineMachineSpec> getLineMachineSpecs2() {
        final List<LineMachineSpec> result = Lists.newArrayList();
        var lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(1);
        lineMachineSpec.setSpindleNum(32);
        List<LineMachineSilkSpec> lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 1, 14));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 2, 13));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 3, 12));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 4, 11));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 5, 10));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 1, 6, 9));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 1, 15));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 2, 16));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 3, 32));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 4, 31));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 5, 30));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 2, 6, 29));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 1, 23));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 2, 24));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 3, 25));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 4, 26));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 5, 27));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 3, 6, 28));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 1, 22));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 2, 21));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 3, 20));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 4, 19));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 5, 18));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.A, 4, 6, 17));

        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 1, 2));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 2, 1));

        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 1, 3));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 2, 4));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 3, 5));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 4, 6));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 5, 7));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 4, 6, 8));

        lineMachineSpec = new LineMachineSpec();
        result.add(lineMachineSpec);
        lineMachineSpec.setOrderBy(2);
        lineMachineSpec.setSpindleNum(32);
        lineMachineSilkSpecs = Lists.newArrayList();
        lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecs);
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 1, 27));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 2, 28));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 3, 29));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 4, 30));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 5, 31));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 1, 6, 32));

        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 1, 26));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 2, 25));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 3, 24));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 4, 23));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 5, 22));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 2, 6, 21));

        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 3, 17));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 4, 18));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 5, 19));
        lineMachineSilkSpecs.add(lineMachineSilkSpec(SilkCarSideType.B, 3, 6, 20));
        return result;
    }

}