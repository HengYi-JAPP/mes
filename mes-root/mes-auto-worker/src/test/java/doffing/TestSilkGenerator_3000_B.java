package doffing;

import com.google.common.collect.ImmutableList;
import com.hengyi.japp.mes.auto.domain.data.SilkCarSideType;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

import static com.hengyi.japp.mes.auto.application.AutoSilkCarModelConfigRegistry.*;
import static io.vertx.config.yaml.YamlProcessor.YAML_MAPPER;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author jzb 2018-11-13
 */
public class TestSilkGenerator_3000_B {

    @Test
    public void testCheckSils() throws Exception {
        final Config config = new Config();
        config.setCorporationCode("3000");
        config.setWorkshopCode("B");
        final SilkCarSpec silkCarSpec = new SilkCarSpec();
        config.setSilkCarSpec(silkCarSpec);
        silkCarSpec.setRow(3);
        silkCarSpec.setCol(5);
        final ImmutableList.Builder<LineMachineSpec> lineMachineSpecsBuilder = ImmutableList.builder();
        for (int row = silkCarSpec.getRow(); row > 0; row--) {
            final LineMachineSpec lineMachineSpec = new LineMachineSpec();
            lineMachineSpec.setOrderBy(4 - row);
            lineMachineSpec.setSpindleNum(10);
            lineMachineSpecsBuilder.add(lineMachineSpec);
            final ImmutableList.Builder<LineMachineSilkSpec> lineMachineSilkSpecsBuilder = ImmutableList.builder();
            for (int spindle = 10; spindle >= 6; spindle--) {
                final LineMachineSilkSpec lineMachineSilkSpec = new LineMachineSilkSpec();
                lineMachineSilkSpec.setSpindle(spindle);
                lineMachineSilkSpec.setSideType(SilkCarSideType.A);
                lineMachineSilkSpec.setRow(row);
                lineMachineSilkSpec.setCol(11 - spindle);
                lineMachineSilkSpecsBuilder.add(lineMachineSilkSpec);
            }
            for (int spindle = 5; spindle >= 1; spindle--) {
                final LineMachineSilkSpec lineMachineSilkSpec = new LineMachineSilkSpec();
                lineMachineSilkSpec.setSpindle(spindle);
                lineMachineSilkSpec.setSideType(SilkCarSideType.B);
                lineMachineSilkSpec.setRow(row);
                lineMachineSilkSpec.setCol(6 - spindle);
                lineMachineSilkSpecsBuilder.add(lineMachineSilkSpec);
            }
            lineMachineSpec.setLineMachineSilkSpecs(lineMachineSilkSpecsBuilder.build());
        }
        config.setLineMachineSpecs(lineMachineSpecsBuilder.build());
        final File file = FileUtils.getFile("/home/mes/auto/auto_doffing_config/3000_B_3X5.yml");
        final String content = YAML_MAPPER.writeValueAsString(config);
        FileUtils.write(file, content, UTF_8);
        config.selfCheck();
    }

}
